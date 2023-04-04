import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Scanner;

import static java.lang.Math.min;

public class matrixproduct {
    public static void printM(int[][] matrix, int size) {
        for (int h=0;h<size; h++){
            for (int w=0;w<size;w++) {
                System.out.print(matrix[h][w] + " ");
            }
            System.out.println();
        }
    }

    public static int[][][] generateM(int width, int heigth){
        int[][][] ret = new int[2][heigth][width];

        for (int h=0;h<heigth; h++){
            for (int w=0;w<width;w++){
                ret[0][h][w]=1;
                ret[1][h][w]=1;
            }
        }

        return ret;
    }

    public static void matrixblockmult(int m_ar, int bksize) {
        Instant inst1;
        Instant inst2;
        
        double [][] pha = new double[m_ar][m_ar];
        double [][] phb = new double[m_ar][m_ar];

        for(int i=0; i<m_ar; i++){
            for(int j=0; j<m_ar; j++){
                pha[i][j] = 1.0;
                phb[i][j] = 1.0+i;
            }
        }
        double [][] c = new double[m_ar][m_ar];


        inst1 = Instant.now();

        int sz=m_ar/bksize;//=size

        /*for(int v=0; v< m_ar; v+=sz){
            for(int z=0; z<m_ar;z+=sz){
                for(int i=0;i<m_ar;i++){
                    for(int j=v;j<((v+sz)>m_ar?m_ar:(v+sz));j++){
                        tp=0;
                        for(int k=z;k<((z+sz)>m_ar?m_ar:(z+sz));k++){
                            tp+=pha[i][k] * phb[k][j];
                        }
                        c[i][j]+=tp;
                    }
                }
            }
        }*/

        /*for(int ii=0; ii< m_ar; ii+=bksize){
            for(int jj=0; jj<m_ar;jj+=bksize){
                for(int kk=0; kk<m_ar;kk+=bksize){
                    for(int i=ii;i<min(ii+bksize,m_ar);i++){
                        for(int j=jj;j<min(jj+bksize,m_ar);j++){
                            for(int k=kk;k<min(kk+bksize,m_ar);k++){
                                c[i][j]+=pha[i][k] * phb[k][j];
                            }
                        }
                    }
                }
            }
        }*/
        //bksize=s=N
        /*for(int jj=0; jj<=sz; jj+=bksize) {
            for (int kk = 1; kk <= sz; kk += bksize) {
                for (int i = 1; i <= sz; i++) {
                    for (int j = jj; j <= min(jj + bksize - 1, sz); j++) {
                        tp = 0;
                        for (int k = kk; k <= min(kk + bksize - 1, sz); k++) {
                            tp += pha[j][k]*phb[i][k];
                        }
                        c[i][j] += tp;
                    }
                }
            }
        }*/

        for(int ii=0; ii<m_ar; ii+=bksize){
	    	for(int jj=0; jj<m_ar; jj+=bksize){
	    		for(int kk=0; kk<m_ar; kk+=bksize){
	    			for(int i=0; i<bksize; i++){
	    				for(int j=0; j<bksize; j++){
	    					for(int k=0; k<bksize; k++){
	    						//phc[(ii+i)*m_ar+jj+j] += pha[(ii+i)*m_ar+kk+k]*phb[(kk+k)*m_ar+jj+j];
                                c[ii+i][jj+j] += pha[ii+i][kk+k]*phb[kk+k][jj+j];
	    					}
	    				}
	    			}
	    		}
	    	}
	    }

        /*for(int jj=0; jj<m_ar; jj+=bksize) {
            for (int kk = 0; kk < m_ar; kk += bksize) {
                for (int i = 0; i < m_ar; i++) {
                    for (int j = jj; j < min(jj + bksize, m_ar); j++) {
                        tp = 0;
                        for (int k = kk; k < min(kk + bksize, m_ar); k++) {
                            tp += pha[i][k]*phb[k][j];
                        }
                        c[i][j] += tp;
                    }
                }
            }
        }*/

        inst2 = Instant.now();

        System.out.println("Elapsed Time: "+ Duration.between(inst1, inst2).toString());

        return;
    }

    public static void matrixlinemult(int m_ar) {
        Instant inst1;
        Instant inst2;

        double [][] pha = new double[m_ar][m_ar];
        double [][] phb = new double[m_ar][m_ar];

        for(int i=0; i<m_ar; i++){
            for(int j=0; j<m_ar; j++){
                pha[i][j] = 1.0;
                phb[i][j] = 1.0+i;
            }
        }

        double [][] c = new double[m_ar][m_ar];

        inst1 = Instant.now();

        for(int row1=0;row1<m_ar;row1++){
            for (int col1=0;col1<m_ar;col1++){
                for(int col2=0;col2<m_ar;col2++) {
                    //printM(c,n);
                    //System.out.println("C"+row1+col2 + " = " + "A"+row1+col1+":"+a[row1][col1] +"  *  B"+col1+col2+":"+b[col1][col2] );
                    c[row1][col2] += pha[row1][col1] * phb[col1][col2];
                }
            }
        }

        inst2 = Instant.now();

        System.out.println("Elapsed Time: "+ Duration.between(inst1, inst2).toString());

        return;
    }

    public static void matrixmult(int m_ar){
        Instant inst1;
        Instant inst2;

        double [][] pha = new double[m_ar][m_ar];
        double [][] phb = new double[m_ar][m_ar];

        for(int i=0; i<m_ar; i++){
            for(int j=0; j<m_ar; j++){
                pha[i][j] = 1.0;
                phb[i][j] = 1.0+i;
            }
        }
        double [][] c = new double[m_ar][m_ar];

        inst1 = Instant.now();
        for (int i=0;i<m_ar;i++){
            for(int j=0;j<m_ar;j++){
                c[i][j]=0;
                for(int k=0;k<m_ar;k++){
                    c[i][j]+=pha[i][k]*phb[k][j];
                }
            }
        }
        inst2 = Instant.now();

        System.out.println("Elapsed Time: "+ Duration.between(inst1, inst2).toString());

        return;
    }

    public static void main(String[] args) {

	int op=1;
	do {
		System.out.println("\n1. Multiplication \n");
		System.out.println("2. Line Multiplication \n");
		System.out.println("3. Block Multiplication \n");
		System.out.println("Selection?: ");
		
        Scanner sc = new Scanner(System.in);
        op = sc.nextInt();

		if (op == 0)
			break;
		System.out.println("Dimensions: lins=cols ? ");
   		int lin = sc.nextInt();

		switch (op){
			case 1: 
				matrixmult(lin);
				break;
			case 2:
				matrixlinemult(lin);  
				break;
			case 3:
				System.out.println("Block Size? ");
				int blockSize = sc.nextInt();
				matrixblockmult(lin, blockSize);  
				break;

		}
	}while (op != 0);

        /*for(int size=600; size<=3000; size+=400){
            int [][][] matxs = generateM(size,size);
            int [][] c = new int[size][size];
            matrixmult(matxs[0],matxs[1],c,size);
        }*/

    }
}