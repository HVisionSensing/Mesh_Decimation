/**
 * for 3x3 matrix
 * @author Andong.Li
 *
 */

public class MatrixInverter
{
   public static void invert(float src[][], float dst[][]) {

      for (int i = 0 ; i < 3 ; i++)
      for (int j = 0 ; j < 3 ; j++) {
         int iu = (i + 1) % 3, iv = (i + 2) % 3;
         int ju = (j + 1) % 3, jv = (j + 2) % 3;
         dst[j][i] = src[iu][ju] * src[iv][jv] - src[iu][jv] * src[iv][ju];
      }


      double det = src[0][0]*dst[0][0] + src[1][0]*dst[0][1] + src[2][0]*dst[0][2];
      for (int i = 0 ; i < 3 ; i++)
      for (int j = 0 ; j < 3 ; j++)
         dst[i][j] /= det;


      for (int i = 0 ; i < 3 ; i++)
         dst[i][3] = -dst[i][0]*src[0][3] - dst[i][1]*src[1][3] - dst[i][2]*src[2][3];


      for (int i = 0 ; i < 4 ; i++)
         dst[3][i] = i < 3 ? 0 : 1;
   }
} 
