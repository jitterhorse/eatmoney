package enums;

import java.util.Random;

public enum Follow{
	  
	  cloth,pin,emit,comment,video,butler;
	  
	  public static Follow randomFollow(){
		    Follow[] vias = {Follow.cloth,Follow.pin,Follow.emit};
		    Random generator = new Random();
		    return vias[generator.nextInt(vias.length)];
		    }
}