package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Classe permettant au client de communiquer avec le serveur
 * @author Tinithrari
 */
public final class JoueurReseauClient extends JoueurHumain{
    
    private Socket socket;
    
    /**
     * Permet de créer un joueur réseau
     * @param nom le nom du joueur
     * @param socket le socket de communication
     * @throws IOException
     */
    public JoueurReseauClient(String nom, Socket socket) throws IOException
    {
        super(nom);
        this.socket = socket;
        send("register " + nom);
        
        String request = receive();
        String[] splitted_request = request.split(" ");
        
        if (splitted_request.length == 2 && splitted_request[0].equals("ack"))
        {
            int num_client = Integer.parseInt(splitted_request[1]);
            this.setNum_joueur(num_client);
        }
        else
            throw new IOException("Requête erronée de la part du serveur " + socket.getInetAddress().toString());
    }

    /**
     * Permet de récupérer le socket du joueur
     * @return le socket du joueur
     */
    public Socket getSocket() {
        return socket;
    }
    
    /**
     * Permet d'envoyer un message aux serveur distant
     * @param message Le message à envoyer au serveur
     */
    @Override
    public void send(String message) throws IOException
    {
        PrintWriter writer = new PrintWriter(socket.getOutputStream());
        writer.println(message);
        writer.flush();
    }

    /**
     * Permet de recevoir un message du serveur distan
     * @return le message du serveur
     */
    @Override
    public String receive() throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String message = reader.readLine();
        return message;
    }
    
    /**
     * Permet de gérer les requêtes
     * @param requete la requête à traiter
     * @throws IOException En cas de problème réseau ou de flux
     */
    public void gererRequete(String requete) throws IOException
    {
        String[] splitted = requete.split(" ");
        
        if (splitted[0].equals("start"))
        {
            commencer(splitted);
        }
        
        else if (splitted[0].equals("play"))
        {
            this.send(commande());
        }
        
        else if (splitted[0].equals("error"))
        {
            erreur(splitted);
        }
        
        else if (splitted[0].equals("move"))
        {
            int numero_joueur = Integer.parseInt(splitted[2]);
            
            System.out.print(joueurs[numero_joueur] + " :");
            
            for (int i = 3; i < splitted.length; i++)
            	System.out.print(" " + splitted[i]);
            
            System.out.print("\n");
        }
        
        else if (splitted[0].equals("ask"))
        {
            String buffer = "respond";
            
            if (suspects.contains(splitted[1]) || armes.contains(splitted[2]) || lieux.contains(splitted[3]))
            {
                Carte card = montrerCarte(new Suspect(splitted[1]), new Arme(splitted[2]), new Lieu(splitted[3]));
                
                if (card != null)
                    send(buffer+" "+card.toString());
                else send(buffer);
            }
        }
        
        else if (splitted[0].equals("info"))
        {
            String affichage = requete.substring(splitted[0].length());
            System.out.println(affichage);
        }
        
        else if (splitted[0].equals("end"))
        {
            if (splitted.length == 1)
                System.out.println("Nobody has won");
            else
            {
                int num_joueur = Integer.parseInt(splitted[1]);
                System.out.println(joueurs[num_joueur] + " has won the game");
            }
            socket.close();
            System.exit(0);
        }             
    }
    
    /**
     * Permet de lancer la vue du joueur
     * @throws IOException En cas de problème réseau
     */
    public void play() throws IOException
    {
        while (true)
        {
            String message = this.receive();
            this.gererRequete(message);
        }
    }
}
