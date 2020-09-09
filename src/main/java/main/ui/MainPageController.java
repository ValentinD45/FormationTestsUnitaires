package main.ui;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.elements.AppSink;
import org.freedesktop.gstreamer.fx.FXImageSink;
import org.freedesktop.gstreamer.message.Message;

import java.net.URL;
import java.nio.ByteOrder;
import java.util.ResourceBundle;

public class MainPageController implements Initializable {

    @FXML
    private AnchorPane anchorIHM;
    @FXML
    private ImageView imageViewCam1;

    Bus busCam1;

    private static final int BUFFER_SIZE                        = 4000000;

    private final int        portCam1                           = 5004;

    private final String     addressMulti                       = "239.192.5.1";

    protected Pipeline       pipelineCam1                       = null;

    private Element          elmt_udpsrcCam1                    = null;

    private Element			 elmt_capsfilter_scaleCam1		    = null;

    private Element			 elmt_rtph264depayCam1				= null;

    private Element			 elmt_vaapih264decCam1				= null;

    private Element			 elmt_videoconvertCam1				= null;

    private Element			 elmt_avdec_h264Cam1				= null;

    private Element          elmt_autovideosinkCam1             = null;

    private Element          elmt_queue0                        = null;
    private Element          elmt_queue1                        = null;
    private Element          elmt_queue2                        = null;
    private Element          elmt_queue3                        = null;

    private AppSink          videoSinkCam1                      = null;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        // Launch information
        System.out.println("Controller launched");

        // Set imageView visible
        imageViewCam1.setVisible(true);

        // PIPELINE CAM 1 CREATION
        buildPipelineCam1();

        // AFFECT PIPELINE 1 --> IMAGEVIEW 1
        var imageSinkCam1 = new FXImageSink(videoSinkCam1);
        imageViewCam1.setPreserveRatio(true);
        imageViewCam1.imageProperty().bind(imageSinkCam1.imageProperty());
    }

    /** PIPE 1 **/

    private void buildPipelineCam1(){

        // Create a pipeline from the text pipeline description
        pipelineCam1 = new Pipeline("PipelineCam1");

        // Create elements
        // Element udpsrc (source udp)
        elmt_udpsrcCam1 = ElementFactory.make("udpsrc", "udpsrc");
        elmt_udpsrcCam1.set("multicast-group", addressMulti);
        elmt_udpsrcCam1.set("auto-multicast", true);
        elmt_udpsrcCam1.set("port", portCam1);
        elmt_udpsrcCam1.set("retrieve-sender-address", "false");
        elmt_udpsrcCam1.set("buffer-size", BUFFER_SIZE);

        // Set Caps
        Caps caps = new Caps("application/x-rtp, media=(string)video, clock-rate=(int)90000, encoding-name=(string)H264, packetization-mode=(string)1, payload=(int)96, a-framerate=(string)25");
        elmt_udpsrcCam1.set("caps", caps);

        // Element rtph264depay
        elmt_rtph264depayCam1 = ElementFactory.make("rtph264depay","rtph264depay");

        // Element vaapih264dec
        elmt_vaapih264decCam1 = ElementFactory.make("vaapih264dec", "vaapih264dec");
        elmt_vaapih264decCam1.set("low-latency", true);

        // Element avdec_h264
        elmt_avdec_h264Cam1 = ElementFactory.make("avdec_h264", "avdec_h264");

        // Element videoconvert
        elmt_videoconvertCam1 = ElementFactory.make("videoconvert", "videoconvert");

        // Video size
        elmt_capsfilter_scaleCam1 = ElementFactory.make("capsfilter", "elmt_capsfilter_scale");
        Caps capsConvert = new Caps("video/x-raw,width=1920,height=1080");
        elmt_capsfilter_scaleCam1.set("caps", capsConvert);


        // Add queus
        elmt_queue0 = ElementFactory.make("queue", "queue0");
        elmt_queue0.set("max-size-buffers", 0);
        elmt_queue1 = ElementFactory.make("queue", "queue1");
        elmt_queue1.set("max-size-buffers", 0);
        elmt_queue2 = ElementFactory.make("queue", "queue2");
        elmt_queue2.set("max-size-buffers", 0);
        elmt_queue3 = ElementFactory.make("queue", "queue3");
        elmt_queue3.set("max-size-buffers", 0);

        this.createAppSinkCam1();
    }

    private void createAppSinkCam1(){

        videoSinkCam1 = new AppSink("GstVideoComponentCam1");
        videoSinkCam1.set("emit-signals", true);
        videoSinkCam1.set("drop", true);

        /** videoSink caps method 1 **/

        final StringBuilder caps = new StringBuilder("video/x-raw,pixel-aspect-ratio=1/1,");

        // JNA creates ByteBuffer using native byte order, set masks according to that.

        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            caps.append("format=BGRx");
        } else {
            caps.append("format=xRGB");
        }

        final Caps theCapsToApply = new Caps(caps.toString());

        videoSinkCam1.setCaps(theCapsToApply);

        elmt_autovideosinkCam1 = videoSinkCam1;
        elmt_autovideosinkCam1.set("sync", "false");

        /** Build Pipeline **/

        // Add elements to pipe
        pipelineCam1.addMany(elmt_udpsrcCam1, elmt_rtph264depayCam1, elmt_vaapih264decCam1, elmt_videoconvertCam1, elmt_autovideosinkCam1);

        // Link elements together
        Element.linkMany(elmt_udpsrcCam1, elmt_rtph264depayCam1, elmt_vaapih264decCam1, elmt_videoconvertCam1, elmt_autovideosinkCam1);

        busCam1 = pipelineCam1.getBus();
        busCam1.connect(new Bus.MESSAGE() {

            @Override
            public void busMessage(Bus arg0, Message arg1) {

                System.out.println(arg1.getStructure());
            }
        });
        pipelineCam1.play();
    }
}