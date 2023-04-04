#include <stdio.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <cmath>
//#include <papi.h>

using namespace std;

#define SYSTEMTIME clock_t

 
void OnMult(int m_ar) 
{
	
	SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	int *pha, *phb, *phc;
		
    pha = (int *)malloc((m_ar * m_ar) * sizeof(int));
	phb = (int *)malloc((m_ar * m_ar) * sizeof(int));
	phc = (int *)malloc((m_ar * m_ar) * sizeof(int));

	for(int i=0; i<m_ar; i++)
		for(int j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (int)(2);



	for(int i=0; i<m_ar; i++)
		for(int j=0; j<m_ar; j++)
			phb[i*m_ar + j] = (int)(i+1);



    Time1 = clock();

	for(int i=0; i<m_ar; i++)
	{	for(int j=0; j<m_ar; j++)
		{	temp = 0;
			for(int k=0; k<m_ar; k++)
			{	
				temp += pha[i*m_ar+k] * phb[k*m_ar+j];
			}
			phc[i*m_ar+j]=temp;
		}
	}


    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (int)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(int i=0; i<6; i++)
	{	for(int j=0; j<6; j++){
			cout << phc[i*m_ar+j] << " ";
		}
		cout << endl;
	}

    free(pha);
    free(phb);
    free(phc);
	
	
}

// add code here for line x line matriz multiplication
void OnMultLine(int m_ar)
{
    SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	

    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			pha[i*m_ar + j] = (double)1.0;



	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			phb[i*m_ar + j] = (double)(i+1);


	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++)
			phc[i*m_ar + j] = 0;

    Time1 = clock();

	for(int row1=0;row1<m_ar;row1++){
		for (int col1=0;col1<m_ar;col1++){
			for(int col2=0;col2<m_ar;col2++) {
				phc[row1*m_ar+col2] += pha[row1*m_ar+col1] * phb[col1*m_ar+col2];
			}
		}
	}


    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(i=0; i<min(10,m_ar); i++){
		for(j=0; j<min(10,m_ar); j++)
			cout << phc[i*m_ar+j] << " ";
		cout << endl;
	}

    free(pha);
    free(phb);
    free(phc);
    
}

// add code here for block x block matriz multiplication
void OnMultBlock(int m_ar, int bkSize)
{
    SYSTEMTIME Time1, Time2;
	
	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;
	

    pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for(i=0; i<m_ar; i++)
		for(j=0; j<m_ar; j++){
			pha[i*m_ar + j] = (double)(2+j);
			phb[i*m_ar + j] = (double)(i+7);
			phc[i*m_ar + j] = 0;
		}

    Time1 = clock();
	int bs= sqrt(bkSize);
	int sz=m_ar/bs;
	int tp;

	/*for(int v=0; v< m_ar; v+=sz){
		for(int z=0; z<m_ar;z+=sz){
			for(int i=0;i<m_ar;i++){
				for(int j=v;j<((v+sz)>m_ar?m_ar:(v+sz));j++){
					tp=0;
					for(int k=z;k<((z+sz)>m_ar?m_ar:(z+sz));k++){
						tp+=pha[i*m_ar+k] * phb[k*m_ar+j];
					}
					phc[i*m_ar+j]+=tp;
				}
			}
		}
	}*/

	for(int ii=0; ii<m_ar; ii+=bkSize){
		for(int jj=0; jj<m_ar; jj+=bkSize){
			for(int kk=0; kk<m_ar; kk+=bkSize){
				for(int i=0; i<bkSize; i++){
					for(int j=0; j<bkSize; j++){
						for(int k=0; k<bkSize; k++){
							phc[(ii+i)*m_ar+jj+j] += pha[(ii+i)*m_ar+kk+k]*phb[(kk+k)*m_ar+jj+j];
						}
					}
				}
			}
		}
	}

	/*sz=m_ar/bkSize;//=size
	for(int jj=0; jj<=sz; jj+=bkSize) {
		for (int kk = 1; kk <= sz; kk += bkSize) {
			for (int i = 1; i <= sz; i++) {
				for (int j = jj; j <= min(jj + bkSize - 1, sz); j++) {
					tp = 0;
					for (int k = kk; k <= min(kk + bkSize - 1, sz); k++) {
						tp += pha[j*m_ar+k]*phb[i*m_ar+k];
					}
					phc[i*m_ar+j] += tp;
				}
			}
		}
	}*/


    Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	cout << "Result matrix: " << endl;
	for(int i=0; i<min(6,m_ar); i++)
	{	for(int j=0; j<min(6,m_ar); j++){
			cout << phc[i*m_ar+j] << " ";
		}
		cout << endl;
	}

    free(pha);
    free(phb);
    free(phc);
    
}



void handle_error (int retval)
{
  //printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
  exit(1);
}

void init_papi() {
  /*int retval = PAPI_library_init(PAPI_VER_CURRENT);
  if (retval != PAPI_VER_CURRENT && retval < 0) {
    printf("PAPI library version mismatch!\n");
    exit(1);
  }
  if (retval < 0) handle_error(retval);

  std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
            << " MINOR: " << PAPI_VERSION_MINOR(retval)
            << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";*/
}


int main (int argc, char *argv[])
{
	OnMult(4);
	OnMultBlock(4096,128);
	/*char c;
	int lin, col, blockSize;
	int op;
	
	int EventSet = PAPI_NULL;
  	long long values[4];
  	int ret;
	

	ret = PAPI_library_init( PAPI_VER_CURRENT );
	if ( ret != PAPI_VER_CURRENT )
		std::cout << "FAIL" << endl;


	ret = PAPI_create_eventset(&EventSet);
		if (ret != PAPI_OK) cout << "ERROR: create eventset" << endl;


	ret = PAPI_add_event(EventSet,PAPI_L1_DCM );
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_DCM" << endl; //data cache miss

	ret = PAPI_add_event(EventSet,PAPI_L1_ICM );
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L1_ICM" << endl; 

	ret = PAPI_add_event(EventSet,PAPI_L2_DCM);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_DCM" << endl;

	ret = PAPI_add_event(EventSet,PAPI_L2_ICM);
	if (ret != PAPI_OK) cout << "ERROR: PAPI_L2_ICM" << endl;


	op=1;
	do {
		cout << endl << "1. Multiplication" << endl;
		cout << "2. Line Multiplication" << endl;
		cout << "3. Block Multiplication" << endl;
		cout << "Selection?: ";
		cin >>op;
		if (op == 0)
			break;
		printf("Dimensions: lins=cols ? ");
   		cin >> lin;

		// Start counting
		ret = PAPI_start(EventSet);
		if (ret != PAPI_OK) cout << "ERROR: Start PAPI" << endl;

		switch (op){
			case 1: 
				OnMult(lin);
				break;
			case 2:
				OnMultLine(lin);  
				break;
			case 3:
				cout << "Block Size? ";
				cin >> blockSize;
				OnMultBlock(lin, blockSize);  
				break;

		}

  		ret = PAPI_stop(EventSet, values);
  		if (ret != PAPI_OK) cout << "ERROR: Stop PAPI" << endl;
  		printf("L1 DCM: %lld \n",values[0]);
		printf("L1 ICM: %lld \n",values[1]);  		
		printf("L2 DCM: %lld \n",values[2]);
  		printf("L2 ICM: %lld \n",values[3]);

		ret = PAPI_reset( EventSet );
		if ( ret != PAPI_OK )
			std::cout << "FAIL reset" << endl; 



	}while (op != 0);

	ret = PAPI_remove_event( EventSet, PAPI_L1_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_remove_event( EventSet, PAPI_L2_DCM );
	if ( ret != PAPI_OK )
		std::cout << "FAIL remove event" << endl; 

	ret = PAPI_destroy_eventset( &EventSet );
	if ( ret != PAPI_OK )
		std::cout << "FAIL destroy" << endl;*/

}