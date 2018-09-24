#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D vidtexture;
uniform sampler2D tracktexture;
uniform sampler2D irisTex;

uniform vec2 iResolution;
uniform int status;
uniform float trackingTimer;
uniform int showIris;

varying vec4 vertColor;
varying vec4 vertTexCoord;


vec3 heatMap(float greyValue) {   
	vec3 heat;      
    heat.r = smoothstep(0.5, 0.8, greyValue);
    if(greyValue >= 0.90) {
    	heat.r *= (1.1 - greyValue) * 5.0;
    }
	if(greyValue > 0.7) {
		heat.g = smoothstep(1.0, 0.2, greyValue);
	} else {
		heat.g = smoothstep(0.0, 0.5, greyValue);
    }    
	heat.b = smoothstep(1.0, 0.0, greyValue);          
    if(greyValue <= 0.3) {
    	heat.b *= greyValue / 0.3;     
    }
	return heat;
}

vec3 striping(float greyValue,vec2 uv){
    vec2 onePixel = vec2(1.0) / vec2(1280.0,720.);
 	float c = 0.;
  	float ln10 = 2.3025850929 * greyValue; // log(10.0);
  	const float minFrequency = 10.0;
  	const float maxFrequency = 22050.0;
 	 float lowLog = log(minFrequency) / ln10;
 	 float highLog = log(maxFrequency) / ln10;
 	 float scale = 1.0 / (highLog - lowLog); 
 	 float frequencyHz = exp(((uv.x / scale) + lowLog) * ln10);
 	 float currentMajorDecade = exp(floor(log(frequencyHz) / ln10) * ln10);	
 	 float nearestMajorDecade = exp(floor((log(frequencyHz) / ln10) + 0.5) * ln10);	
 	 float nearestMinorDecade = floor((frequencyHz / currentMajorDecade) + 0.5) * currentMajorDecade;	
  	float nearestSubMinorDecade = floor((frequencyHz / (currentMajorDecade * 0.1)) + 0.5) * (currentMajorDecade * 0.1);	
  	float ignoreFirstAndLastXFactor = step(onePixel.x, uv.x) * (1.0 - step(1.0 - onePixel.x, uv.x));  
  	c = mix(c, 0.0625, smoothstep(onePixel.x, 0.0, abs((((log(nearestSubMinorDecade) / ln10) - lowLog) * scale) - uv.x)) * ignoreFirstAndLastXFactor);
  	c = mix(c, 0.25, smoothstep(onePixel.x, 0.0, abs((((log(nearestMinorDecade) / ln10) - lowLog) * scale) - uv.x)) * ignoreFirstAndLastXFactor);
  	c = mix(c, 1.0, smoothstep(onePixel.x, 0.0, abs((((log(nearestMajorDecade) / ln10) - lowLog) * scale) - uv.x)) * ignoreFirstAndLastXFactor);
    c+= max(greyValue,0.3);
    
    return vec3(c,c,c);
    
    
}

// Use these parameters to fiddle with settings
float step = 1.0;

float intensity(in vec4 color){
	return sqrt((color.x*color.x)+(color.y*color.y)+(color.z*color.z));
}

vec3 sobel(float stepx, float stepy, vec2 center){
	// get samples around pixel
    float tleft = intensity(texture(vidtexture,center + vec2(-stepx,stepy)));
    float left = intensity(texture(vidtexture,center + vec2(-stepx,0)));
    float bleft = intensity(texture(vidtexture,center + vec2(-stepx,-stepy)));
    float top = intensity(texture(vidtexture,center + vec2(0,stepy)));
    float bottom = intensity(texture(vidtexture,center + vec2(0,-stepy)));
    float tright = intensity(texture(vidtexture,center + vec2(stepx,stepy)));
    float right = intensity(texture(vidtexture,center + vec2(stepx,0)));
    float bright = intensity(texture(vidtexture,center + vec2(stepx,-stepy)));

 
    float x = tleft + 2.0*left + bleft - tright - 2.0*right - bright;
    float y = -tleft - 2.0*top - tright + bleft + 2.0 * bottom + bright;
    float color = sqrt((x*x) + (y*y));
    return vec3(color,color,color);
 }
    


void main()
{

    vec2 uv = vertTexCoord.st; //fragCoord/iResolution.xy;

   
    vec4 col = vec4(texture(vidtexture,uv).rgb,1.);

    
    /////////////////////////////// FRAME1
    vec2 uv2 =  uv * 6.;
    uv2 -= vec2(0.2,0.2);
   
    vec4 col2 = vec4(texture(vidtexture,uv2).rgb,1.);
    col2.rgb = heatMap(1.-(col2.r + col2.g + col2.b / 3.));
    
    if(uv2.x < 0.0) col2 = vec4(0,0,0,0);
    else if(uv2.x > 0.99) col2 = vec4(0,0,0,0);
    if(uv2.y < 0.0) col2 = vec4(0,0,0,0);
    else if(uv2.y > 0.99) col2 = vec4(0,0,0,0);
        
    if(uv2.x > 0. && uv2.x < 0.99 && uv2.y > 0.0 && uv2.y < 0.99) col = vec4(0,0,0,1);

  
        
    /////////////////////////////// FRAME2
    vec2 uv3 =  uv * 6.;
    uv3 -= vec2(0.2,1.2);
   
    vec4 col3 = vec4(texture(vidtexture,uv3).rgb,1.);
    col3.rgb = heatMap((col3.r + col3.g + col3.b / 4.));
    
    if(uv3.x < 0.0) col3 = vec4(0,0,0,0);
    else if(uv3.x > 0.99) col3 = vec4(0,0,0,0);
    if(uv3.y < 0.0) col3 = vec4(0,0,0,0);
    else if(uv3.y > 0.99) col3 = vec4(0,0,0,0);
        
    if(uv3.x > 0. && uv3.x < 0.99 && uv3.y > 0.0 && uv3.y < 0.99) col = vec4(0,0,0,1);
     

        
     /////////////////////////////// FRAME4
    vec2 uv5 =  uv * 6.;
    uv5 -= vec2(0.2,2.2);
   
    vec4 col5 = vec4(sobel(step/1280., step/720., uv5),1);
    
    
    if(uv5.x < 0.0) col5 = vec4(0,0,0,0);
    else if(uv5.x > 0.99) col5 = vec4(0,0,0,0);
    if(uv5.y < 0.0) col5 = vec4(0,0,0,0);
    else if(uv5.y > 0.99) col5 = vec4(0,0,0,0);
        
    if(uv5.x > 0. && uv5.x < 0.99 && uv5.y > 0.0 && uv5.y < 0.99) col = vec4(0,0,0,1);   
 
      /*
     /////////////////////////////// FRAME3
    vec2 uv4 =  uv * 6.;
    uv4 -= vec2(0.2,3.2);
   
    vec4 col4 = vec4(texture(vidtexture,uv3).rgb,1.);
    col4.rgb = striping((col4.r + col4.g + col4.b / 3.),uv4);
    
    if(uv4.x < 0.0) col4 = vec4(0,0,0,0);
    else if(uv4.x > 0.99) col4 = vec4(0,0,0,0);
    if(uv4.y < 0.0) col4 = vec4(0,0,0,0);
    else if(uv4.y > 0.99) col4 = vec4(0,0,0,0);
        
    if(uv4.x > 0. && uv4.x < 0.99 && uv4.y > 0.0 && uv4.y < 0.99) col = vec4(0,0,0,0);    
    */
 
 
      /////////////////////////////// FRAME5
    vec2 uv6 =  uv * 6.;
    uv6 -= vec2(4.7,0.2);
   
    vec4 col6 = texture2D(tracktexture,vec2(uv6.s,1.-uv6.t));
    
    
    if(uv6.x < 0.0) col6 = vec4(0,0,0,0);
    else if(uv6.x > 0.99) col6 = vec4(0,0,0,0);
    if(uv6.y < 0.0) col6 = vec4(0,0,0,0);
    else if(uv6.y > 0.99) col6 = vec4(0,0,0,0);
        
    if(uv6.x > 0. && uv6.x < 0.99 && uv6.y > 0.0 && uv6.y < 0.99) col = vec4(0,0,0,1);         
        
        
    // Output to screen
    if(status==0){
    	gl_FragColor = col + col2 + col3 + col5 + col6;
	    if(trackingTimer < 1.){
	    	 gl_FragColor += texture2D(tracktexture,vec2(vertTexCoord.s,1.-vertTexCoord.t));
	    }
	    if(showIris == 1){
	    	float sx = 1280.0/250.0;
	    	float aspect = 1280.0 / 720.0;
	    	vec4 irC = texture2D(irisTex,vec2(-4.,-0.6) + vec2((vertTexCoord.s*sx),(vertTexCoord.t*(sx / aspect))));
	    	if(irC.a != 0) gl_FragColor = vec4(0,0,0,1) + irC;
	    }
    }
    else if(status==1){
    	gl_FragColor = texture(vidtexture,uv);
    }
    
}