import java.net.Socket;
import java.net.ServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.lang.Thread;
// Exceptions
import java.io.IOException;
import java.lang.InterruptedException;
import java.io.EOFException;
import java.lang.UnsupportedOperationException;

/**
 *	@author David Madrigal Buendía
 *	@version 1.0
 */
class TokenRing
{
	// Constants
	private static final int NUMBER_NODES = 6;
	private static final int INITIAL_PORT = 50000;
	
	public static void main(String[] args)
	{
		if (args.length != 1){
            System.err.println("Falta un argumento");
            System.exit(0);
        }
		
		int node = Integer.valueOf(args[0]);
		System.out.println("Soy el nodo " + node);
		
		try
		{
			menu(node);
		}catch(Exception e)
		{
			System.out.println(e);
		}
	}
	
	/**
	 * To create a Thread that send data to the next node.
	 * @version 1.0
	 */
	static class Worker extends Thread
	{
		int number_nodes;
		int initial_port;
		int node;
		short token;
		
        Worker(int initial_port, int number_nodes, int node, short token)
		{
            this.initial_port = initial_port;
			this.number_nodes = number_nodes;
			this.node = node;
			this.token = token;
        }
		
        public void run()
		{
			SSLSocketFactory ssl_client = (SSLSocketFactory) SSLSocketFactory.getDefault();
			Socket connect;
			int port = initial_port + ((node + 1) % number_nodes);
			// Puertos de 50000 + nodo + 1 (a conectar)
			// Si es el último nodo conecta al primer nodo por el módulo
			do
			{
				try
				{
					// Conexión al puerto
					connect = ssl_client.createSocket("localhost", port);
					break;
				}catch(IOException | UnsupportedOperationException e)
				{
					// Esperamos 200 milisegundos para reintentar la conexión
					try
					{
						Thread.sleep(200);
					}catch(InterruptedException r)
					{
						System.out.println(r.getMessage());
					}
				}
			}while(true);
			
			System.out.println("Conexión segura establecida");
			token++;

            try
			{
                DataOutputStream out_token = new DataOutputStream(connect.getOutputStream());
                out_token.writeShort(token);
				System.out.println("Enviando token con valor: " + token);

                out_token.close();
				connect.close();
            }catch (IOException e){
                System.err.println(e.getMessage());
            }
        }
    }
	
	/**
     * Method to recives an number of node, then creates SSL conection for server, and client by Worker (Thread)
     * @param node Number of node
     * @exception IOException if DataInputStream can't recieve data.
	 * @exception EOFException signals that an end of file or end of stream has been reached unexpectedly during input.
     * @exception InterruptedException if any thread has interrupted the current thread.
	 */
	public static void menu(int node) throws IOException, EOFException, InterruptedException
	{
		short token = 0;
		boolean initialize = false;
		SSLServerSocketFactory ssl_server = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        ServerSocket server = ssl_server.createServerSocket(50000 + node);
        
		if(node == 0)
		{
			initialize = true;
		}
		do
		{
			if(!initialize)
			{
				Socket connect = server.accept();
				
				DataInputStream in_token = new DataInputStream(connect.getInputStream());
				token = in_token.readShort();
				in_token.close();
				connect.close();
				if(node == 0 && token >= 500)
				{
					break;
				}
			}else
			{ // Nodo 0 - inicio
				initialize = false;
			}
			
			Worker v = new Worker(INITIAL_PORT, NUMBER_NODES, node, token);
			v.start();
	        v.join();
		}while(true);
		
		System.out.println("Valor final del token: " + token);
		System.exit(0);
	}
}