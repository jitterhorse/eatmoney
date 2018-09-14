package gamePTZ;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

public class UDPClient
{

   DatagramSocket clientSocket;	
   //boolean bw = false;
   public boolean ismovingLR = false;
   public boolean ismovingUD = false;
   public boolean isZoomIn = false;
   public boolean isZoomOut = false;
   
   byte[] speed = {(byte)0x01,(byte)0x02,(byte)0x03,(byte)0x04,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x09,(byte)0x10,(byte)0x11,(byte)0x12,(byte)0x13,(byte)0x14,(byte)0x15,(byte)0x16,(byte)0x17,(byte)0x18};
   byte[] zoomin = {(byte)0x20,(byte)0x21,(byte)0x22,(byte)0x23,(byte)0x24,(byte)0x25,(byte)0x26,(byte)0x27};
   byte[] zoomout = {(byte)0x30,(byte)0x31,(byte)0x32,(byte)0x33,(byte)0x34,(byte)0x35,(byte)0x36,(byte)0x37};
   byte[] presets = {(byte)0x01,(byte)0x02,(byte)0x03,(byte)0x04,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x09,(byte)0x10};
   
   int zoomstate = 0;
   int zoomspeed = 0;
   Thread thread = new Thread();
   
   public UDPClient(){
     // byte[] sendData = new byte[1024];
      //byte[] receiveData = new byte[1024];
      //String sentence = inFromUser.readLine();

      //byte[] sendData = new byte[] {(byte)0x81,(byte)0x01,(byte)0x04,(byte)0x3F,(byte)0x02,(byte)0x5F,(byte)0xFF};
      //byte[] sendData = new byte[] {(byte)0x81,(byte)0x01,(byte)0x04,(byte)0x63,(byte)0x04,(byte)0xFF};
 
      //DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      //clientSocket.receive(receivePacket);
      //String modifiedSentence = new String(receivePacket.getData());
      //System.out.println("FROM SERVER:" + modifiedSentence);
      //clientSocket.close();
	   getZoomState();
   }
   
   public void sendCommand(byte[] sendData) throws Exception{
	   
	   DatagramSocket clientSocket = new DatagramSocket();
	   InetAddress IPAddress = InetAddress.getByName("192.168.1.88");
	   clientSocket.connect(IPAddress,1259);
	   clientSocket.setSoTimeout(1000);
	   
	   byte[] commonData = new byte[] {(byte)0x81,(byte)0x01};
	   
	   ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
	   outputStream.write( commonData);
	   outputStream.write( sendData );

	   byte c[] = outputStream.toByteArray( );
	   
	    DatagramPacket sendPacket = new DatagramPacket(c, c.length);
	    try {
			clientSocket.send(sendPacket);
		} 
	    catch (SocketException e1) {
	        System.out.println("Socket closed " + e1);
	    }
	      catch (IOException e) {
			e.printStackTrace();
		}
	    clientSocket.close();
   }
   
   public int receiveState() throws Exception{
 
	   DatagramSocket clientSocket = new DatagramSocket();
	   InetAddress IPAddress = InetAddress.getByName("192.168.1.88");
	   clientSocket.connect(IPAddress,1259);
	   clientSocket.setSoTimeout(1000);
	   
	   byte[] sendData = new byte[] {(byte)0x47,(byte)0xFF};
	   byte[] commonData = new byte[] {(byte)0x81,(byte)0x09,(byte)0x04};
	   
	   
	   ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
	   outputStream.write(commonData);
	   outputStream.write(sendData);

	   byte c[] = outputStream.toByteArray( );
	   
	    DatagramPacket sendPacket = new DatagramPacket(c, c.length);
	    byte [] vals = new byte[4];
	    try {
			clientSocket.send(sendPacket);
			byte[] buffer = new byte[7];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			clientSocket.receive(packet);
			buffer = packet.getData();
			vals[0] = buffer[2];
			vals[1] = buffer[3];
			vals[2] = buffer[4];
			vals[3] = buffer[5];       			
		}catch (SocketException e1) {
	        System.out.println("Socket closed " + e1);
	    }
	    catch (IOException e) {
			e.printStackTrace();
		}
	    clientSocket.close();
	    return bytesToShort(vals);
        //max = 778
   }  
   
   
   public short bytesToShort(byte[] bytes) {
	     return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getShort();
	}
   
   public static final long unsignedIntToLong(byte[] b) 
   {
       long l = 0;
       l |= b[0] & 0xFF;
       l <<= 8;
       l |= b[1] & 0xFF;
       l <<= 8;
       l |= b[2] & 0xFF;
       l <<= 8;
       l |= b[3] & 0xFF;
       return l;
   }
      
   
   public void zoom(String inout) {
	   byte[] sendData = {};
	   
	   if(inout.equals("in")) {
		   zoomspeed = (int)((1.-((float)zoomstate/(float)778))*(zoomin.length-1));
		   sendData = new byte[] {(byte)0x04,(byte)0x07,zoomin[zoomspeed],(byte)0xFF};
		   isZoomIn = true;
		   isZoomOut = false;
	   }
	   else if(inout.equals("out")) {
		   zoomspeed = (int)((1.-((float)zoomstate/(float)778))*(zoomout.length-1));
		   sendData = new byte[] {(byte)0x04,(byte)0x07,zoomout[zoomspeed],(byte)0xFF};
		   isZoomOut = true;
		   isZoomIn = false;
	   }
	   else if(inout.equals("stop")){
		   sendData = new byte[] {(byte)0x04,(byte)0x07,(byte)0x00,(byte)0xFF};
		   isZoomIn = false;
		   isZoomOut = false;
	   }
   
	   try {
			sendCommand(sendData);
			if(!thread.isAlive() && (isZoomOut == true || isZoomIn == true)) {
				//System.out.println("start thread");
				thread = new Thread(new MyRunnable());
				thread.start();
			}
			else if(thread.isAlive() && (isZoomOut == false && isZoomIn == false)) {
				//System.out.println("stop thread");
				thread.interrupt();
			}
		


			
		} catch (Exception e) {
			e.printStackTrace();
		}
	   
	   /*
	    * Stop 81 01 04 07 00 FF
	    * Tele (Standard) 81 01 04 07 02 FF 
	    * Wide (Standard) 81 01 04 07 03 FF
	    * 
	    * Tele (Variable) 81 01 04 07 2p FF
	    * Wide (Variable) 81 01 04 07 3p FF
	    * 
	    * p = 0(low) - 7(high) 
	    */
   }
   
   public class MyRunnable implements Runnable {

	    public void run(){   
	       while (!Thread.currentThread().isInterrupted()) {
	    	   try {
	    		   	int newstate = receiveState();
	    			zoomstate = (newstate > 0 && newstate < 778) ? newstate : zoomstate;
	    			//System.out.println(zoomstate);
	    			int zoomspeednew = (int)((1.-((float)zoomstate/(float)778))*zoomin.length);
	    			if(zoomspeednew != zoomspeed) {
	    				if(isZoomIn == true) zoom("in");
	    				else if(isZoomOut == true) zoom("out");
	    			}
	    		} catch (Exception e1) {
	    				e1.printStackTrace();
	    		}
	    	   
	       }
	    }
	  }

   public void getZoomState() {
	   try {
		zoomstate = receiveState();
		//System.out.println(zoomstate);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	   
   }
   
   public void moveNew(float leftright,float updown) {
	   boolean moveLR = false;
	   boolean moveUD = false;
	   byte[] sendData = {};
	   //directions = (left right), (up down)
	   if(leftright != 0) {
		   moveLR = true;
	   }
	   if(updown != 0) {
		   moveUD = true;
	   }
	   //System.out.println(leftright + " / " + updown);
	   //if zero
	   if(moveLR == false && moveUD == false) {
		   this.stop();
	   }
	 //create move command LR + UD
	   if(moveLR == true && moveUD == true) {
		   //create updown byte
		   byte ud = (byte)0x01; //up
		   if(updown > 0) ud = (byte)0x02;
		   int speednew = (int)((1.-((float)zoomstate/(float)778))*(speed.length-10));
		   byte udspeed = speed[speednew];
		   
		   //create leftright byte
		   byte lr = (byte)0x01; //left
		   if(leftright > 0) lr = (byte)0x02;
		   byte lrspeed = speed[speednew];
		   
		   sendData = new byte[] {(byte)0x06,(byte)0x01,lrspeed,udspeed,lr,ud,(byte)0xFF};
		   ismovingLR = true;
		   ismovingUD = true;
	   }
	   else if(moveLR == true) {
		   byte lr = (byte)0x01; //left
		   if(leftright > 0) lr = (byte)0x02;
		   int speednew = (int)((1.-((float)zoomstate/(float)778))*(speed.length-10));
		   byte lrspeed = speed[speednew];
		   
		   sendData = new byte[] {(byte)0x06,(byte)0x01,lrspeed,(byte)0x01,lr,(byte)0x03,(byte)0xFF};
		   ismovingLR = true;
		   ismovingUD = false;
	   }
	   
	   else if(moveUD == true) {
		   //create updown byte
		   byte ud = (byte)0x01; //up
		   if(updown > 0) ud = (byte)0x02;
		   int speednew = (int)((1.-((float)zoomstate/(float)778))*(speed.length-10));
		   byte udspeed = speed[speednew];
		   
		   sendData = new byte[] {(byte)0x06,(byte)0x01,(byte)0x01,udspeed,(byte)0x03,ud,(byte)0xFF};
		   ismovingUD = true;
		   ismovingLR = false;
	   }
	   else if (moveLR == false && moveUD == false) {
		   this.stop();
		   
		   }

	   try {
		sendCommand(sendData);
	} catch (Exception e) {
		e.printStackTrace();
	}
	   
}
   
   
   public void stop() {
	   byte[] sendData = new byte[] {(byte)0x06,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x03,(byte)0x03,(byte)0xFF};
	   try {
		   sendCommand(sendData);
		   ismovingLR = false;
		   ismovingUD = false;
	   } catch (Exception e) {
		e.printStackTrace();
	   }
   }
   
   public void autoFocus() {
	   byte[] sendData = new byte[] {(byte)0x04,(byte)0x38,(byte)0x10,(byte)0xFF};
	   try {
			sendCommand(sendData);
			//System.out.println("save preset");
		} catch (Exception e) {
			e.printStackTrace();
		}
   }
   
   public void savePreset(int number) {
	   byte preset = presets[number];
	   //81 01 04 3F 01 pp FF
	   byte[] sendData = new byte[] {(byte)0x04,(byte)0x3F,(byte)0x01,preset,(byte)0xFF};
	   try {
			sendCommand(sendData);
			//System.out.println("save preset");
		} catch (Exception e) {
			e.printStackTrace();
		}
   }
 
   public void recallPreset(int number) {
	   byte preset = presets[number];
	   //81 01 04 3F 00 pp FF
	   byte[] sendData = new byte[] {(byte)0x04,(byte)0x3F,(byte)0x02,preset,(byte)0xFF};
	   try {
			sendCommand(sendData);
			//System.out.println("recall preset");
		} catch (Exception e) {
			e.printStackTrace();
		}
   }
   
   
}