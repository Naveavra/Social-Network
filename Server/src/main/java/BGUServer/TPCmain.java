package BGUServer;

import bgu.spl.net.BGUNet;
import bgu.spl.net.srv.Server;

public class TPCmain {
    public static void main(String[] args) throws Exception {
        BGUNet _bgu = new BGUNet();
        if(args.length>0){
            Server.threadPerClient(
                    Integer.parseInt(args[0]),
                    ()->new MessagingProtocolBGU(),
                    messageEncoderDecoderBGR::new
            ).serve();
        }else throw new Exception("not enough arguments");
    }

}
