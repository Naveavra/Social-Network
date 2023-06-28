package BGUServer;

import bgu.spl.net.BGUNet;
import bgu.spl.net.srv.Server;

public class ReactorMain {
    public static void main(String[] args) throws Exception{
        BGUNet _db = new BGUNet();
        int port = 7777;
        int threads_counter = 2;
        if(args.length>1){
            port = Integer.parseInt(args[0]);
            threads_counter = Integer.parseInt(args[1]);
        }
        else throw new Exception("not enough arguments");
        Server.reactor(
                threads_counter,
                port,
                ()->new MessagingProtocolBGU(),
                messageEncoderDecoderBGR::new
        ).serve();

    }
}
