package videoCapture;

import static org.bytedeco.javacpp.opencv_face.drawFacemarks;
import static org.bytedeco.javacpp.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.equalizeHist;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.CvType;
import org.bytedeco.javacpp.opencv_core.GpuMat;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point2f;
import org.bytedeco.javacpp.opencv_core.Point2fVector;
import org.bytedeco.javacpp.opencv_core.Point2fVectorVector;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_core.UMat;
import org.bytedeco.javacpp.opencv_cudaobjdetect.CudaCascadeClassifier;
import org.bytedeco.javacpp.opencv_face.Facemark;
import org.bytedeco.javacpp.opencv_face.FacemarkLBF;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

import eatmoney.eatMoneyMain;
import processing.core.PImage;

public class FaceMark implements Runnable{


	CudaCascadeClassifier faceDetector,faceDetector2,faceDetector3,faceDetector4;
	Facemark facemark;
	float scale = 4.0f;
	
	byte [] bArray;
	int [] iArray;
	int pixCnt1, pixCnt2;
	
	static PImage cap;
	
	GpuMat greyGpu ;
	
	static eatMoneyMain emm;
	
	
	public int detections  = 0;
	
	public Point2fVectorVector landmarks = new Point2fVectorVector();
	
	public boolean detection = true;
	
	    public FaceMark(eatMoneyMain _emm, PImage camera) {
	    	
	    	Loader.load(org.bytedeco.javacpp.opencv_stitching.class);
	    	
	    	greyGpu = new GpuMat(1280,720,opencv_core.CV_8UC4);
	    	
	    	cap = camera;
	    	
	    	opencv_core.printCudaDeviceInfo(0);

	    	emm = _emm;
	    	
	        faceDetector = CudaCascadeClassifier.create("C:\\opencv_3.0\\opencv\\sources\\data\\haarcascades_cuda\\haarcascade_frontalface_alt2.xml");
	        faceDetector2 = CudaCascadeClassifier.create("C:\\opencv_3.0\\opencv\\sources\\data\\haarcascades_cuda\\haarcascade_eye.xml");
	        faceDetector3 = CudaCascadeClassifier.create("C:\\opencv_3.0\\opencv\\sources\\data\\haarcascades_cuda\\haarcascade_upperbody.xml");
	        faceDetector4 =CudaCascadeClassifier.create("C:\\opencv_3.0\\opencv\\sources\\data\\haarcascades\\palm.xml");
	        
	        // Create an instance of Facemark
	        facemark = FacemarkLBF.create();
	 
	        // Load landmark detector 
	        facemark.loadModel("C:\\Users\\jitterhorse\\workspace\\cppCV2\\src\\main\\java\\org\\jitterhorse\\mav\\cppCV2\\lbfmodel.yaml");
	        
	        (new Thread(this)).start();
	    }
	    
	    

	    public void setDetection(boolean state) {
	    	detection = state;
	    	System.out.println(detection);
	    }
	    
	    
		public void detect() {
				
	    		int w = cap.width;
	    		int h = cap.height;
	    		
	    		int pixCnt = w*h*4;
	    	    Mat frame = new Mat(new Size(w, h), opencv_core.CV_8UC4, Scalar.all(0));

	    		ByteBuffer b = ByteBuffer.allocate(pixCnt);
	    		b.asIntBuffer().put(cap.pixels);
	    		b.rewind();
	
	    	    
	    	    frame.getByteBuffer().put(b.array());
	    		

		        Mat frameS = new Mat();
	    	
	            UMat gray = new UMat ();
	            UMat graySmall = new UMat();
	            frame.copyTo(gray);
	       
	            Size target = new Size(160,120);
	            opencv_imgproc.resize(gray, graySmall, target );

	            cvtColor(graySmall, graySmall, COLOR_BGR2GRAY);
	            equalizeHist( graySmall, graySmall );
	            greyGpu.upload(graySmall);
	            
          
	            GpuMat faces = new GpuMat();
	            GpuMat eyes =  new GpuMat();
	            GpuMat bodies =  new GpuMat();
	            GpuMat hands =  new GpuMat();
	            
	            faceDetector.detectMultiScale(greyGpu, faces);
	            faceDetector2.detectMultiScale(greyGpu, eyes);
	            faceDetector3.detectMultiScale(greyGpu, bodies);
	            faceDetector4.detectMultiScale(greyGpu, hands);
	            
	            RectVector faceVector = new RectVector();
	        	RectVector eyeVector = new RectVector();
	        	RectVector bodyVector = new RectVector();
	        	RectVector handVector = new RectVector();
	        	faceDetector.convert(faces, faceVector);
	        	faceDetector2.convert(eyes, eyeVector);
	        	faceDetector3.convert(bodies, bodyVector);
	        	faceDetector4.convert(hands, handVector);  
	            
	            //System.out.println ("Faces detected: "+faces.size());
	            // Verify is at least one face is detected
	            // With some Facemark algorithms it crashes if there is no faces
	            if (!faces.empty()) {
	        
	                // Variable for landmarks. 
	                // Landmarks for one face is a vector of points
	                // There can be more than one face in the image.
	                landmarks = new Point2fVectorVector();

	                // Run landmark detector
	                frame.copyTo(frameS);
	                opencv_imgproc.resize(frame,frameS,target);

	                boolean success = facemark.fit(frameS, faceVector, landmarks);

	                if(success) {
	                    // If successful, render the landmarks on the face
	                    for (long i = 0; i < landmarks.size(); i++) {
	                        Point2fVector v = landmarks.get(i);
	                        for(long j = 0; j < v.size();j++) {
		                        Point2f xop = v.get(j);
		                        xop.x(xop.x()*scale);
		                        xop.y(xop.y()*scale);
		                        //System.out.println("after: " + xop.x() + " / " + xop.y());
	                        }
	                        drawFacemarks(frame, v, Scalar.YELLOW);
	                    }
	                }
	            }
	            
	            else {
	            	landmarks = new Point2fVectorVector();
	            }
	            
	            
	          
	            
	        	int detections_num = (int) faceVector.size();
	        	int detections_num_eye = (int) eyeVector.size();
	        	int detections_num_bodies = (int) bodyVector.size();
	        	int detections_num_hands = (int) handVector.size();
	        	
	        	detections =  detections_num;
	        	/*
	            if (!faces.empty()) {

	            	for(int i = 0; i < detections_num; i++) {
	            		Scalar tr = new Scalar(0,255,0,255);
	            		Rect r = faceVector.get(i);
	            		Rect sr = new Rect((int)(r.x()*scale),(int)(r.y()*scale),(int)(r.width()*scale),(int)(r.height()*scale));
		        		opencv_imgproc.rectangle(frame,sr, tr);
		        	}
	            }
	            
	            
	            if (!eyes.empty()) {
	  
	            	for(int i = 0; i < detections_num_eye; i++) {
	            		Scalar tr = new Scalar(255,0,0,255);
	            		Rect r = eyeVector.get(i);
	            		Rect sr = new Rect((int)(r.x()*scale),(int)(r.y()*scale),(int)(r.width()*scale),(int)(r.height()*scale));
		        		opencv_imgproc.rectangle(frame,sr, tr);
		        	}
	            	
	            	
	            }
	            
	            if (!bodies.empty()) {
	    
	            	for(int i = 0; i < detections_num_bodies; i++) {
	            		Scalar tr = new Scalar(0,0,255,255);
	            		Rect r = bodyVector.get(i);
	            		Rect sr = new Rect((int)(r.x()*scale),(int)(r.y()*scale),(int)(r.width()*scale),(int)(r.height()*scale));
		        		opencv_imgproc.rectangle(frame,sr, tr);
		        	}
	            	
	            	
	            }
	            
	            if (!hands.empty()) {
	  
	            	for(int i = 0; i < detections_num_hands; i++) {
	            		Scalar tr = new Scalar(128,255,0,255);
	            		Rect r = handVector.get(i);
	            		Rect sr = new Rect((int)(r.x()*scale),(int)(r.y()*scale),(int)(r.width()*scale),(int)(r.height()*scale));
		        		opencv_imgproc.rectangle(frame,sr, tr);
		        	}
	            	
	            }
	            */
	   }



		@Override
		public void run() {
			System.out.println("fm started");
			while(true) {
				if(detection == true) {
					detect();
				}
				else if(detection == false) {
				}
			}
			
		}


}
	

