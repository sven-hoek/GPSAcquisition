package de.amidar;

public class LinpackFloat {

	  public static void main(String[] args)
	  {
	    LinpackFloat l = new LinpackFloat();
//	    for(int i = 0; i< 100; i++)
	    l.run_benchmark();
	  }

	  final float abs (float d) {
	    return (d >= 0) ? d : -d;
	  }

	  double second_orig = -1;

	  double second()
	  {
	    if (second_orig==-1) {
	      second_orig = System.currentTimeMillis();
	    }
	    return (System.currentTimeMillis() - second_orig);
	  }

	  public void run_benchmark() 
	  {
	    double mflops_result = 0.0f;
	    float residn_result = 0.0f;
	    double time_result = 0.0f;
	    float eps_result = 0.0f;

	    float a[][] = new float[500][501];
	    float b[] = new float[500];
	    float adummy[][] = new float[500][501];
	    float bdummy[] = new float[500];
	    float x[] = new float[500];
	    float cray,ops,norma,normx;
	    float resid;
	    double time, total;
	    float kf;
	    int n,ndummy,i,ntimes,info,lda,ldadummy,ldaa,kflops;
	    int ipvt[] = new int[500];
	    int ipvtdummy[] = new int[500];
	    
	    //float mflops_result;
	    //float residn_result;
	    //float time_result;
	    //float eps_result;

	    lda = 501;
	    ldadummy = lda;
	    ldaa = 500;
	    cray = .056f;
	    n = 400;
	    ndummy = n;
	    
	    ops = (2.0e0f*(n*n*n))/3.0f + 2.0f*(n*n);
	    
//	    matgen(adummy,ldadummy,ndummy,bdummy);
//	    dgefa(adummy,ldadummy,ndummy,ipvtdummy);
//	    dgesl(adummy,ldadummy,ndummy,ipvtdummy,bdummy,0);

	    
	    norma = matgen(a,lda,n,b);
	    time = second();
	    info = dgefa(a,lda,n,ipvt);
	    dgesl(a,lda,n,ipvt,b,0);
	    total = second() - time;
	    
	    for (i = 0; i < n; i++) {
	      x[i] = b[i];
	    }
	    norma = matgen(a,lda,n,b);
	    for (i = 0; i < n; i++) {
	      b[i] = -b[i];
	    }
	    dmxpy(n,b,n,lda,x,a);
	    resid = 0.0f;
	    normx = 0.0f;
	    for (i = 0; i < n; i++) {
	      resid = (resid > abs(b[i])) ? resid : abs(b[i]);
	      normx = (normx > abs(x[i])) ? normx : abs(x[i]);
	    }
	    
	    eps_result = epslon((float)1.0);
	/*

	    residn_result = resid/( n*norma*normx*eps_result );
	    time_result = total;
	    mflops_result = ops/(1.0e6*total);

	    return ("Mflops/s: " + mflops_result +
		    "  Time: " + time_result + " secs" +
		    "  Norm Res: " + residn_result +
		    "  Precision: " + eps_result);
	*/
		residn_result = resid/( n*norma*normx*eps_result );
		residn_result += 0.005; // for rounding
		residn_result = (int)(residn_result*100);
		residn_result /= 100;

		time_result = total;
		time_result += 0.005; // for rounding
		time_result = (int)(time_result*100);
		time_result /= 100;

		mflops_result = ops/(1.0e6f*total);
		mflops_result += 0.0005; // for rounding
		mflops_result = (int)(mflops_result*1000000);
		mflops_result /= 1000;

	    System.out.println("Mflops/s: " + mflops_result +
		    "  Time: " + time_result + " secs" +
		    "  Norm Res: " + residn_result +
		    "  Precision: " + eps_result);
	  }
	  

	  
	  final float matgen (float a[][], int lda, int n, float b[])
	  {
		  float norma;
		  int init, i, j;

		  init = 1325;
		  norma = 0.0f;
		  /*  Next two for() statements switched.  Solver wants
	matrix in column order. --dmd 3/3/97
		   */
		  for (i = 0; i < n; i++) {
			  for (j = 0; j < n; j++) {
				  init = 3125*init % 65536;
				  a[j][i] = (init - 32768.0f)/16384.0f;
				  norma = (a[j][i] > norma) ? a[j][i] : norma;
			  }
		  }
		  for (i = 0; i < n; i++) {
			  b[i] = 0.0f;
		  }
		  for (j = 0; j < n; j++) {
			  for (i = 0; i < n; i++) {
				  b[i] += a[j][i];
			  }
		  }

		  return norma;
	  }
	  

	  
	  /*
	    dgefa factors a float precision matrix by gaussian elimination.
	    
	    dgefa is usually called by dgeco, but it can be called
	    directly with a saving in time if  rcond  is not needed.
	    (time for dgeco) = (1 + 9/n)*(time for dgefa) .
	    
	    on entry
	    
	    a       float precision[n][lda]
	    the matrix to be factored.
	    
	    lda     integer
	    the leading dimension of the array  a .
	    
	    n       integer
	    the order of the matrix  a .
	    
	    on return
	    
	    a       an upper triangular matrix and the multipliers
	    which were used to obtain it.
	    the factorization can be written  a = l*u  where
	    l  is a product of permutation and unit lower
	    triangular matrices and  u  is upper triangular.
	    
	    ipvt    integer[n]
	    an integer vector of pivot indices.
	    
	    info    integer
	    = 0  normal value.
	    = k  if  u[k][k] .eq. 0.0 .  this is not an error
	    condition for this subroutine, but it does
	    indicate that dgesl or dgedi will divide by zero
	    if called.  use  rcond  in dgeco for a reliable
	    indication of singularity.
	    
	    linpack. this version dated 08/14/78.
	    cleve moler, university of new mexico, argonne national lab.
	    
	    functions
	    
	    blas daxpy,dscal,idamax
	  */
	  final int dgefa( float a[][], int lda, int n, int ipvt[])
	  {
		  float[] col_k, col_j1, col_j2, col_j3, col_j4, col_j5=null, col_j6=null, col_j7=null, col_j8=null;
		  float t,t1,t2,t3,t4,t5=0,t6=0,t7=0,t8=0;
		  int j,k,kp1,l,nm1;
		  int info;

		  
		  float[] tt = new float[n];
		  // gaussian elimination with partial pivoting

		  info = 0;
		  nm1 = n - 1;
		  if (nm1 >=  0) {
			  for (k = 0; k < nm1; k++) {
				  col_k = a[k];
				  kp1 = k + 1;

				  // find l = pivot index

				  l = idamax(n-k,col_k,k,1) + k;
				  ipvt[k] = l;

				  // zero pivot implies this column already triangularized

				  if (col_k[l] != 0) {

					  // interchange if necessary

					  if (l != k) {
						  t = col_k[l];
						  col_k[l] = col_k[k];
						  col_k[k] = t;
					  }

					  // compute multipliers

					  t = -1.0f/col_k[k];
					  dscal(n-(kp1),t,col_k,kp1,1);
					  
					  // row elimination with column indexing
					  for (j = kp1; j < n; j=j+8) {
						  col_j1 = a[j];
						  col_j2 = a[j+1];
						  col_j3 = a[j+2];
						  col_j4 = a[j+3];
						  col_j5 = a[j+4];
						  col_j6 = a[j+5];
						  col_j7 = a[j+6];
						  col_j8 = a[j+7];
						  t1 = col_j1[l];
						  t2 = col_j2[l];
						  t3 = col_j3[l];
						  t4 = col_j4[l];
						  t5 = col_j5[l];
						  t6 = col_j6[l];
						  t7 = col_j7[l];
						  t8 = col_j8[l];
						  if (l != k) {
							  col_j1[l] = col_j1[k];
							  col_j1[k] = t1;
							  col_j2[l] = col_j2[k];
							  col_j2[k] = t2;
							  col_j3[l] = col_j3[k];
							  col_j3[k] = t3;
							  col_j4[l] = col_j4[k];
							  col_j4[k] = t4;
							  col_j5[l] = col_j5[k];
							  col_j5[k] = t5;
							  col_j6[l] = col_j6[k];
							  col_j6[k] = t6;
							  col_j7[l] = col_j7[k];
							  col_j7[k] = t7;
							  col_j8[l] = col_j8[k];
							  col_j8[k] = t8;
						  }
//						  for(int p = kp1; p < n;  p = p +8){
//							  int lim = n-p;
//							  if(lim > 8){
//								  lim = 8;
//							  }
							  daxpy2(n-kp1,t1,t2,t3,t4,t5,t6,t7,t8,col_k,kp1,1,
									  col_j1,col_j2,col_j3,col_j4,col_j5,col_j6,col_j7,col_j8,kp1,1);
//						  }
					  }
				  }
				  else {
					  info = k;
				  }
			  }
		  }
		  ipvt[n-1] = n-1;
		  if (a[(n-1)][(n-1)] == 0) info = n-1;

		  return info;
	  }

	  
	  
	  /*
	    dgesl solves the float precision system
	    a * x = b  or  trans(a) * x = b
	    using the factors computed by dgeco or dgefa.
	  
	    on entry
	  
	    a       float precision[n][lda]
	    the output from dgeco or dgefa.
	  
	    lda     integer
	    the leading dimension of the array  a .
	    
	    n       integer
	    the order of the matrix  a .
	  
	    ipvt    integer[n]
	    the pivot vector from dgeco or dgefa.

	    b       float precision[n]
	    the right hand side vector.
	    
	    job     integer
	    = 0         to solve  a*x = b ,
	    = nonzero   to solve  trans(a)*x = b  where
	    trans(a)  is the transpose.
	    
	    on return
	    
	    b       the solution vector  x .
	    
	    error condition
	    
	    a division by zero will occur if the input factor contains a
	    zero on the diagonal.  technically this indicates singularity
	    but it is often caused by improper arguments or improper
	    setting of lda .  it will not occur if the subroutines are
	    called correctly and if dgeco has set rcond .gt. 0.0
	    or dgefa has set info .eq. 0 .
	    
	    to compute  inverse(a) * c  where  c  is a matrix
	    with  p  columns
	    dgeco(a,lda,n,ipvt,rcond,z)
	    if (!rcond is too small){
	    for (j=0,j<p,j++)
	    dgesl(a,lda,n,ipvt,c[j][0],0);
	    }
	    
	    linpack. this version dated 08/14/78 .
	    cleve moler, university of new mexico, argonne national lab.
	    
	    functions
	    
	    blas daxpy,ddot
	  */
	  final void dgesl( float a[][], int lda, int n, int ipvt[], float b[], int job)
	  {
	    float t;
	    int k,kb,l,nm1,kp1;

	    nm1 = n - 1;
	    if (job == 0) {

	      // job = 0 , solve  a * x = b.  first solve  l*y = b

	      if (nm1 >= 1) {
		for (k = 0; k < nm1; k++) {
		  l = ipvt[k];
		  t = b[l];
		  if (l != k){
		    b[l] = b[k];
		    b[k] = t;
		  }
		  kp1 = k + 1;
		  daxpy(n-(kp1),t,a[k],kp1,1,b,kp1,1);
		}
	      }

	      // now solve  u*x = y

	      for (kb = 0; kb < n; kb++) {
		k = n - (kb + 1);
		b[k] /= a[k][k];
		t = -b[k];
		daxpy(k,t,a[k],0,1,b,0,1);
	      }
	    }
	    else {

	      // job = nonzero, solve  trans(a) * x = b.  first solve  trans(u)*y = b

	      for (k = 0; k < n; k++) {
		t = ddot(k,a[k],0,1,b,0,1);
		b[k] = (b[k] - t)/a[k][k];
	      }

	      // now solve trans(l)*x = y 

	      if (nm1 >= 1) {
		for (kb = 1; kb < nm1; kb++) {
		  k = n - (kb+1);
		  kp1 = k + 1;
		  b[k] += ddot(n-(kp1),a[k],kp1,1,b,kp1,1);
		  l = ipvt[k];
		  if (l != k) {
		    t = b[l];
		    b[l] = b[k];
		    b[k] = t;
		  }
		}
	      }
	    }
	  }



	  /*
	    constant times a vector plus a vector.
	    jack dongarra, linpack, 3/11/78.
	  */
	  final void daxpy( int n, float da, float dx[], int dx_off, int incx,
		      float dy[], int dy_off, int incy)
	  {
	    int i,ix,iy;

	    if ((n > 0) && (da != 0)) {
	      if (incx != 1 || incy != 1) {

		// code for unequal increments or equal increments not equal to 1

		ix = 0;
		iy = 0;
		if (incx < 0) ix = (-n+1)*incx;
		if (incy < 0) iy = (-n+1)*incy;
		for (i = 0;i < n; i++) {
		  dy[iy +dy_off] += da*dx[ix +dx_off];
		  ix += incx;
		  iy += incy;
		}
//		return;
		int dumm = 1;
	      } else {

		// code for both increments equal to 1

		for (i=0; i < n; i++)
		  dy[i +dy_off] += da*dx[i +dx_off];
		int dumm2 = 2;
	      }
	    }
	  }
	  
	  final void daxpy2( int n, float da1, float da2, float da3, float da4, float da5, float da6, float da7, float da8, float dx[], int dx_off, int incx,
			  float dy1[], float dy2[],float dy3[],float dy4[],float dy5[], float dy6[],float dy7[],float dy8[], int dy_off, int incy)
	  {
		  int i,ix,iy;

//		  if ((n > 0)) {
//			  if (incx != 1 || incy != 1) {
//
//				  // code for unequal increments or equal increments not equal to 1
//
//				  ix = 0;
//				  iy = 0;
//				  if (incx < 0) ix = (-n+1)*incx;
//				  if (incy < 0) iy = (-n+1)*incy;
//				  for (i = 0;i < n; i++) {
//					  dy1[iy +dy_off] += da1*dx[ix +dx_off];
//					  dy2[iy +dy_off] += da2*dx[ix +dx_off];
//					  dy3[iy +dy_off] += da3*dx[ix +dx_off];
//					  dy4[iy +dy_off] += da4*dx[ix +dx_off];
//					  dy5[iy +dy_off] += da5*dx[ix +dx_off];
//					  dy6[iy +dy_off] += da6*dx[ix +dx_off];
//					  dy7[iy +dy_off] += da7*dx[ix +dx_off];
//					  dy8[iy +dy_off] += da8*dx[ix +dx_off];
//					  
//					  ix += incx;
//					  iy += incy;
//				  }
//				  int dummy = 99;
////				  return;
//			  } else {

				  // code for both increments equal to 1

				  for (i=0; i < n; i=i+4){
					  dy1[i +dy_off] += da1*dx[i +dx_off];
					  dy2[i +dy_off] += da2*dx[i +dx_off];
					  dy3[i +dy_off] += da3*dx[i +dx_off];
					  dy4[i +dy_off] += da4*dx[i +dx_off];
					  dy1[i+1 +dy_off] += da1*dx[i+1 +dx_off];
					  dy2[i+1 +dy_off] += da2*dx[i+1 +dx_off];
					  dy3[i+1 +dy_off] += da3*dx[i+1 +dx_off];
					  dy4[i+1 +dy_off] += da4*dx[i+1 +dx_off];
					  dy1[i+2 +dy_off] += da1*dx[i+2 +dx_off];
					  dy2[i+2 +dy_off] += da2*dx[i+2 +dx_off];
					  dy3[i+2 +dy_off] += da3*dx[i+2 +dx_off];
					  dy4[i+2 +dy_off] += da4*dx[i+2 +dx_off];
					  dy1[i+3 +dy_off] += da1*dx[i+3 +dx_off];
					  dy2[i+3 +dy_off] += da2*dx[i+3 +dx_off];
					  dy3[i+3 +dy_off] += da3*dx[i+3 +dx_off];
					  dy4[i+3 +dy_off] += da4*dx[i+3 +dx_off];
//					  dy1[i+4 +dy_off] += da1*dx[i+4 +dx_off];
//					  dy2[i+4 +dy_off] += da2*dx[i+4 +dx_off];
//					  dy3[i+4 +dy_off] += da3*dx[i+4 +dx_off];
//					  dy4[i+4 +dy_off] += da4*dx[i+4 +dx_off];
//					  dy1[i+5 +dy_off] += da1*dx[i+5 +dx_off];
//					  dy2[i+5 +dy_off] += da2*dx[i+5 +dx_off];
//					  dy3[i+5 +dy_off] += da3*dx[i+5 +dx_off];
//					  dy4[i+5 +dy_off] += da4*dx[i+5 +dx_off];
//					  dy1[i+6 +dy_off] += da1*dx[i+6 +dx_off];
//					  dy2[i+6 +dy_off] += da2*dx[i+6 +dx_off];
//					  dy3[i+6 +dy_off] += da3*dx[i+6 +dx_off];
//					  dy4[i+6 +dy_off] += da4*dx[i+6 +dx_off];
//					  dy1[i+7 +dy_off] += da1*dx[i+7 +dx_off];
//					  dy2[i+7 +dy_off] += da2*dx[i+7 +dx_off];
//					  dy3[i+7 +dy_off] += da3*dx[i+7 +dx_off];
//					  dy4[i+7 +dy_off] += da4*dx[i+7 +dx_off];
					  dy5[i +dy_off] += da5*dx[i +dx_off];
					  dy6[i +dy_off] += da6*dx[i +dx_off];
					  dy7[i +dy_off] += da7*dx[i +dx_off];
					  dy8[i +dy_off] += da8*dx[i +dx_off];
					  dy5[i+1 +dy_off] += da5*dx[i+1 +dx_off];
					  dy6[i+1 +dy_off] += da6*dx[i+1 +dx_off];
					  dy7[i+1 +dy_off] += da7*dx[i+1 +dx_off];
					  dy8[i+1 +dy_off] += da8*dx[i+1 +dx_off];
					  dy5[i+2 +dy_off] += da5*dx[i+2 +dx_off];
					  dy6[i+2 +dy_off] += da6*dx[i+2 +dx_off];
					  dy7[i+2 +dy_off] += da7*dx[i+2 +dx_off];
					  dy8[i+2 +dy_off] += da8*dx[i+2 +dx_off];
					  dy5[i+3 +dy_off] += da5*dx[i+3 +dx_off];
					  dy6[i+3 +dy_off] += da6*dx[i+3 +dx_off];
					  dy7[i+3 +dy_off] += da7*dx[i+3 +dx_off];
					  dy8[i+3 +dy_off] += da8*dx[i+3 +dx_off];
//					  dy5[i+4 +dy_off] += da5*dx[i+4 +dx_off];
//					  dy6[i+4 +dy_off] += da6*dx[i+4 +dx_off];
//					  dy7[i+4 +dy_off] += da7*dx[i+4 +dx_off];
//					  dy8[i+4 +dy_off] += da8*dx[i+4 +dx_off];
//					  dy5[i+5 +dy_off] += da5*dx[i+5 +dx_off];
//					  dy6[i+5 +dy_off] += da6*dx[i+5 +dx_off];
//					  dy7[i+5 +dy_off] += da7*dx[i+5 +dx_off];
//					  dy8[i+5 +dy_off] += da8*dx[i+5 +dx_off];
//					  dy5[i+6 +dy_off] += da5*dx[i+6 +dx_off];
//					  dy6[i+6 +dy_off] += da6*dx[i+6 +dx_off];
//					  dy7[i+6 +dy_off] += da7*dx[i+6 +dx_off];
//					  dy8[i+6 +dy_off] += da8*dx[i+6 +dx_off];
//					  dy5[i+7 +dy_off] += da5*dx[i+7 +dx_off];
//					  dy6[i+7 +dy_off] += da6*dx[i+7 +dx_off];
//					  dy7[i+7 +dy_off] += da7*dx[i+7 +dx_off];
//					  dy8[i+7 +dy_off] += da8*dx[i+7 +dx_off];
				  }
//				  int dummy2= 8888;
//			  }
//		  }
	  }


	  /*
	    forms the dot product of two vectors.
	    jack dongarra, linpack, 3/11/78.
	  */
	  final float ddot( int n, float dx[], int dx_off, int incx, float dy[],
		       int dy_off, int incy)
	  {
	    float dtemp;
	    int i,ix,iy;

	    dtemp = 0;

	    if (n > 0) {
	      
	      if (incx != 1 || incy != 1) {

		// code for unequal increments or equal increments not equal to 1

		ix = 0;
		iy = 0;
		if (incx < 0) ix = (-n+1)*incx;
		if (incy < 0) iy = (-n+1)*incy;
		for (i = 0;i < n; i++) {
		  dtemp += dx[ix +dx_off]*dy[iy +dy_off];
		  ix += incx;
		  iy += incy;
		}
	      } else {

		// code for both increments equal to 1
		
		for (i=0;i < n; i++)
		  dtemp += dx[i +dx_off]*dy[i +dy_off];
	      }
	    }
	    return(dtemp);
	  }

	  
	  
	  /*
	    scales a vector by a constant.
	    jack dongarra, linpack, 3/11/78.
	  */
	  final void dscal( int n, float da, float dx[], int dx_off, int incx)
	  {
	    int i,nincx;

	    if (n > 0) {
	      if (incx != 1) {

		// code for increment not equal to 1

		nincx = n*incx;
		for (i = 0; i < nincx; i += incx)
		  dx[i +dx_off] *= da;
	      } else {

		// code for increment equal to 1

		for (i = 0; i < n; i++)
		  dx[i +dx_off] *= da;
	      }
	    }
	  }

	  
	  
	  /*
	    finds the index of element having max. absolute value.
	    jack dongarra, linpack, 3/11/78.
	  */
	  final int idamax( int n, float dx[], int dx_off, int incx)
	  {
	    float dmax, dtemp;
	    int i, ix, itemp=0;

	    if (n < 1) {
	      itemp = -1;
	    } else if (n ==1) {
	      itemp = 0;
	    } else if (incx != 1) {

	      // code for increment not equal to 1

	      dmax = abs(dx[0 +dx_off]);
	      ix = 1 + incx;
	      for (i = 1; i < n; i++) {
		dtemp = abs(dx[ix + dx_off]);
		if (dtemp > dmax)  {
		  itemp = i;
		  dmax = dtemp;
		}
		ix += incx;
	      }
	    } else {

	      // code for increment equal to 1

	      itemp = 0;
	      dmax = abs(dx[0 +dx_off]);
	      for (i = 1; i < n; i++) {
		dtemp = abs(dx[i + dx_off]);
		if (dtemp > dmax) {
		  itemp = i;
		  dmax = dtemp;
		}
	      }
	    }
	    return (itemp);
	  }


	  
	  /*
	    estimate unit roundoff in quantities of size x.
	    
	    this program should function properly on all systems
	    satisfying the following two assumptions,
	    1.  the base used in representing dfloating point
	    numbers is not a power of three.
	    2.  the quantity  a  in statement 10 is represented to
	    the accuracy used in dfloating point variables
	    that are stored in memory.
	    the statement number 10 and the go to 10 are intended to
	    force optimizing compilers to generate code satisfying
	    assumption 2.
	    under these assumptions, it should be true that,
	    a  is not exactly equal to four-thirds,
	    b  has a zero for its last bit or digit,
	    c  is not exactly equal to one,
	    eps  measures the separation of 1.0 from
	    the next larger dfloating point number.
	    the developers of eispack would appreciate being informed
	    about any systems where these assumptions do not hold.
	    
	    *****************************************************************
	    this routine is one of the auxiliary routines used by eispack iii
	    to avoid machine dependencies.
	    *****************************************************************
	  
	    this version dated 4/6/83.
	  */
	  final float epslon (float x)
	  {
	    float a,b,c,eps;

	    a = 4.0e0f/3.0e0f;
	    eps = 0;
	    while (eps == 0) {
	      b = a - 1.0f;
	      c = b + b + b;
	      eps = abs(c-1.0f);
	    }
	    return(eps*abs(x));
	  }

	  

	  /*
	    purpose:
	    multiply matrix m times vector x and add the result to vector y.
	    
	    parameters:
	    
	    n1 integer, number of elements in vector y, and number of rows in
	    matrix m
	    
	    y float [n1], vector of length n1 to which is added
	    the product m*x
	    
	    n2 integer, number of elements in vector x, and number of columns
	    in matrix m
	    
	    ldm integer, leading dimension of array m
	    
	    x float [n2], vector of length n2
	    
	    m float [ldm][n2], matrix of n1 rows and n2 columns
	  */
	  final void dmxpy ( int n1, float y[], int n2, int ldm, float x[], float m[][])
	  {
	    int j,i;

	    // cleanup odd vector
	    for (j = 0; j < n2; j++) {
	      for (i = 0; i < n1; i++) {
		y[i] += x[j]*m[j][i];
	      }
	    }
	  }

	}
