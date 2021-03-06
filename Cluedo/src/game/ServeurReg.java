package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tinithrari
 */
public class ServeurReg{
    
    private ServerSocket sSocket;
    private LinkedList<Joueur> clients;
    private int max_connection;
    private int nb_connection;
    
    /**
     * 
     * @param port /port du serveur
     * @param max_connection /nombre maximal de connections 
     * @param embedded /indique si le serveur est imbriqué ou non
     * @throws IOException 
     */
    public ServeurReg(int port, int max_connection, boolean embedded) throws IOException
    {
        this.sSocket = new ServerSocket(port);
        this.clients = new LinkedList<Joueur>();
        this.max_connection = max_connection;
        if (!embedded)
            this.nb_connection = 0;
        else
        {
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter your name");
            String nom = sc.nextLine();
            
            clients.add(new JoueurHumain(nom));
            
            clients.get(0).setNum_joueur(0);
            this.nb_connection = 1;
        }
    }

    public ServeurReg() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Lance l'enregistrement des joueurs au serveurs tant que nombre de joueurs
     * est inférieure au nombre de connections maximales
     */
    public void run() 
    {    
        while (nb_connection < max_connection)
        {
            Socket s = new Socket();
            try 
            {
                s = sSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String request = reader.readLine();
                String[] splitted_request = request.split(" ");
                String nom;
                
                if (splitted_request.length > 0)
                {
                    if (splitted_request[0].equals("register"))
                    {
                        if (splitted_request.length == 2)
                        {
                            nom = splitted_request[1];
                            JoueurReseauServeur j = new JoueurReseauServeur(s, nom);
                            try 
                            {
                                j.send("ack " + this.getNb_connection());
                                j.setNum_joueur(nb_connection);
                                this.setNb_connection(this.getNb_connection() + 1);
                                System.out.println(nom + " is connected");
                                clients.add(j);
                            }
                            catch (IOException e)
                            {
                                System.err.println("Erreur de communication avec le client");
                                s.close();
                            }
                        }
                    }
                }
            } 
            catch (IOException ex) 
            {
                if (s.isBound())
                    System.err.println("Erreur lors de la création du serveur, fermeture...");
            }
        }
    }
    
    public void addIA(JoueurOrdi ordi)
    {
    	ordi.setNum_joueur(nb_connection);
    	clients.add(ordi);
    	this.setNb_connection(this.getNb_connection() + 1);
    }
    
    /**
     * @return la liste des joueurs connectés
     */
    public synchronized LinkedList<Joueur> getClients() {
        return clients;
    }

    /**
     * @return le nombre de connections maximales
     */
    public synchronized int getMax_connection() {
        return max_connection;
    }
    
    /**
     * modifie le nombre de connections maximales
     * @param max_connection 
     */
    public synchronized void setMax_connection(int max_connection) {
        this.max_connection = max_connection;
    }

    /**
     * @return le nombre de connections
     */
    public synchronized int getNb_connection() {
        return nb_connection;
    }
    
    /**
     * modifie le nombre de connections
     * @param nb_connection 
     */
    public synchronized void setNb_connection(int nb_connection) {
        this.nb_connection = nb_connection;
    }
}
