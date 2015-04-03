#pragma version(1)
#pragma rs java_package_name(com.wordsaretoys.iconist)

const int MaxColors = 32;
const int MaxCoeffs = 4;

float sine[65536];
float4 colors[MaxColors];
float4 coeffs[MaxCoeffs];
float2 size;
int mirrorX;
int mirrorY;
float2 aspect;
int phase;

uchar4 __attribute__((kernel)) root(uint32_t x, uint32_t y) {

	float fx = 2 * x / size.x;
	float fy = 2 * y / size.y;
	float px = fx - 1;
	float py = fy - 1;

	if (mirrorX)
		fx = fabs(px);
	
	if (mirrorY) 
		fy = fabs(py); 

	fx *= aspect.x;
	fy *= aspect.y; 

	float s = 0.0f;
	for (int i = 0; i < MaxCoeffs; i++) {
		float t0 = coeffs[i].x * fx + coeffs[i].w * s;
		float t1 = coeffs[i].y * fy + coeffs[i].z * s;
		int i0 = (int)(t0 * 20861) & 65535;
		int i1 = (int)(phase + t1 * 20861) & 65535;
		s = (sine[i0] + sine[i1]) * 0.5f;
	}

	float p = px * px + py * py;
	s = s * clamp(1.0f - p, 0.0f, 1.0f);
	
	int ci = s * MaxColors;
	return rsPackColorTo8888(colors[ci]);
}