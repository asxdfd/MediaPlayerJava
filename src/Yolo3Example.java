import org.bytedeco.javacpp.*;
import org.bytedeco.opencv.global.opencv_dnn;

import org.bytedeco.opencv.opencv_dnn.*;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.javacpp.indexer.*;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;

import java.util.*;
import java.io.*;
/**
 * Created by Jacky on 2019/7/9.
 */
public class Yolo3Example {

    public void Yolo3(){
        Mat img = imread("1.jpg");

        //setting blob, size can be:320/416/608
        //opencv blob setting can check here https://github.com/opencv/opencv/tree/master/samples/dnn#object-detection
        Mat blob = opencv_dnn.blobFromImage(img, 1 / 255.0, new Size(320, 320), new Scalar(), true, false, CV_32F);
//        Mat blob = opencv_dnn.blobFromImage(img, 1.0, new Size(608, 608), new Scalar(), true, false,CV_8U);

        //load model and config, if you got error: "separator_index < line.size()", check your cfg file, must be something wrong.
        String cfg = "yolo/yolov3.cfg";
        String model = "yolo/yolov3.weights";
        Net net = opencv_dnn.readNetFromDarknet(cfg, model);
        //set preferable
        net.setPreferableBackend(3);
            /*
            0:DNN_BACKEND_DEFAULT
            1:DNN_BACKEND_HALIDE
            2:DNN_BACKEND_INFERENCE_ENGINE
            3:DNN_BACKEND_OPENCV
             */
        net.setPreferableTarget(0);
            /*
            0:DNN_TARGET_CPU
            1:DNN_TARGET_OPENCL
            2:DNN_TARGET_OPENCL_FP16
            3:DNN_TARGET_MYRIAD
            4:DNN_TARGET_FPGA
             */

        //input data
        net.setInput(blob);

        //get output layer name
        StringVector outNames = net.getUnconnectedOutLayersNames();
        //create mats for output layer
        //MatVector outs = outNames.Select(_ => new Mat()).ToArray();

        MatVector outs = new MatVector();
        for(int i=0;i<outNames.size();i++){
            outs.put(new Mat());
        }

        //forward model
        net.forward(outs, outNames);

        //get result from all output
        float threshold = 0.5f;       //for confidence
        float nmsThreshold = 0.3f;    //threshold for nms
        GetResult(outs, img, threshold, nmsThreshold,true);
    }

    private void GetResult(MatVector output, Mat image, float threshold, float nmsThreshold, boolean nms)
    {
        nms = true;
        //for nms
        ArrayList<Integer> classIds = new ArrayList<>();
        ArrayList<Float> confidences = new ArrayList<>();
        ArrayList<Float> probabilities = new ArrayList<>();
        ArrayList<Rect2d> rect2ds = new ArrayList<>();
        //Rect2dVector boxes = new Rect2dVector();
        try{
            int w = image.cols();
            int h = image.rows();
            /*
             YOLO3 COCO trainval output
             0 1 : center                    2 3 : w/h
             4 : confidence                  5 ~ 84 : class probability
            */
            int prefix = 5;   //skip 0~4
            /**/
            for(int k=0;k<output.size();k++)
            {
                Mat prob = output.get(k);
                final FloatRawIndexer probIdx = prob.createIndexer();
                for (int i = 0; i < probIdx.rows(); i++)
                {
                    float confidence = probIdx.get(i, 4);
                    if (confidence > threshold)
                    {
                        //get classes probability
                        DoublePointer minVal= new DoublePointer();
                        DoublePointer maxVal= new DoublePointer();
                        Point min = new Point();
                        Point max = new Point();
                        minMaxLoc(prob.rows(i).colRange(prefix, prob.cols()), minVal, maxVal, min, max, null);
                        int classes = max.x();
                        float probability = probIdx.get(i, classes + prefix);

                        if (probability > threshold) //more accuracy, you can cancel it
                        {
                            //get center and width/height
                            float centerX = probIdx.get(i, 0) * w;
                            float centerY = probIdx.get(i, 1) * h;
                            float width = probIdx.get(i, 2) * w;
                            float height = probIdx.get(i, 3) * h;

                            if (!nms)
                            {
                                // draw result (if don't use NMSBoxes)
                                continue;
                            }

                            //put data to list for NMSBoxes
                            classIds.add(classes);
                            confidences.add(confidence);
                            probabilities.add(probability);
                            rect2ds.add(new Rect2d(centerX, centerY, width, height));
                        }
                    }
                }
            }

            if (!nms) return;

            //using non-maximum suppression to reduce overlapping low confidence box
            IntPointer indices = new IntPointer(confidences.size());
            Rect2dVector boxes = new Rect2dVector();
            for(int i=0;i<rect2ds.size();i++){
                boxes.push_back(rect2ds.get(i));
            }

            FloatPointer con = new FloatPointer(confidences.size());
            float[] cons = new float[confidences.size()];
            for(int i=0;i<confidences.size();i++){
                cons[i] = confidences.get(i);
            }
            con.put(cons);

            opencv_dnn.NMSBoxes(boxes, con, threshold, nmsThreshold, indices); //只会修改前2个参数，后面不动？

            List<String> list = new ArrayList<String>();
            FileInputStream fis = new FileInputStream("yolo/coco.names");
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
            String[] Labels = list.toArray(new String[list.size()]);
            br.close();
            isr.close();
            fis.close();
            //Console.WriteLine($"NMSBoxes drop {confidences.Count - indices.Length} overlapping result.");

            for (int m=0;m<indices.sizeof();m++)
            {
                int i = indices.get(m);
                //System.out.println(i);
                Rect2d box = boxes.get(i);
                String res = "name="+Labels[classIds.get(i)]+" classIds="+classIds.get(i)+" confidences="+confidences.get(i)+" probabilities="+probabilities.get(i);
                res += " box.x="+box.x() + " box.y="+box.y() + " box.width="+box.width() + " box.height="+box.height();
                System.out.println(res);
            }
        }catch(Exception e){
            System.out.println("GetResult error:" + e.getMessage());
        }


    }

    public static void main(String[] args) {
        Yolo3Example be =  new Yolo3Example();
        be.Yolo3();
    }
}