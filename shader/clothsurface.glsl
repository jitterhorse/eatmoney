float rand(float n){return fract(sin(n) * 43758.5453123);}

float noise(float p){
	float fl = floor(p);
  float fc = fract(p);
	return mix(rand(fl), rand(fl + 1.0), fc);
}


float rnd(vec2 x)
{
    int n = int(x.x * 40.0 + x.y * 6400.0);
    n = (n << 13) ^ n;
    return 1.0 - float( (n * (n * n * 15731 + 789221) + \
             1376312589) & 0x7fffffff) / 1073741824.0;
}


void main( out vec4 O, in vec2 C)
{
	vec2 p = C.xy / iResolution.xy;
    p.x *= (floor(sin(iTime*0.2) * 21.0))/7.0;
    p.y *= (floor(cos(iTime*0.32) * 50.0));
    
	O.r = pow(1.-0.5* cos(C.x + rnd(p)*100.), 0.05*sin(iTime*10.0));
    O.g = pow(cos(C.y)*112., 0.2*cos(iTime*1.));
    O.b = pow(sin(C.x + rnd(p)*10.)*112.*sin(iTime*0.1), 0.2*sin(iTime));
    
    
    O  *= smoothstep(sin(iTime*0.2) + sin(iTime) *0.2, 1. - sin(iTime) *0.1,p.x);
    
    O  *= smoothstep(sin(iTime*0.2) + cos(iTime) *0.25, 0.54 - sin(iTime) *0.4,p.y);
    
    O = min(O,vec4(0.2+sin(iTime*0.2),0.4+cos(iTime*0.04),0.5 * sin(iTime*0.04) - 0.5 *cos(iTime*0.54),1));
    
    if(O.r+O.g+O.b == 0.)  O = vec4(1,1,1,1);
    
    
    float cellr = 2.+ floor((p.x+0.3*sin(iTime*0.2)+cos(iTime*0.12)) * 10.);
    float cellg = floor((p.x*sin(iTime*0.032)+cos(iTime*0.042)) * 17.);
    float cellb = floor((p.y*sin(iTime*0.02)+cos(iTime*0.02)) * 8.);
    
    O /= vec4(noise(cellr),noise(cellg),noise(cellb),0.1);                   
                       
    
    
}