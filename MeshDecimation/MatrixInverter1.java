/**
 * for matrix 4x4
 * @author Andong.Li
 *
 */


public class MatrixInverter1 {
   static float[][] tmp = new float[4][4];
   static float[] max = new float[4];
   static int[] index = new int[4];

   public static void invert(float[][] src, float[][] dst) {
      int i, j, k, m = 0, n = 4;

      for (i = 0 ; i < n ; i++) {
         index[i] = i;
         max [i] = 0;
         for (j = 0 ; j < n ; j++) {
            max[i] = Math.max(max[i], Math.abs(src[i][j]));
            tmp[i][j] = i == j ? 1 : 0;
         }
      }

      for (j = 0 ; j < n - 1 ; j++) {
         float t = 0;
         for (i = j ; i < n ; i++) {
            float s = Math.abs(src[index[i]][j]) / max[index[i]];
            if (s > t) {
               t = s;
               m = i;
            }
         }
         int swap = index[j];
         index[j] = index[m];
         index[m] = swap;

         for (i = j + 1 ; i < n ; i++) {
            float p = src[index[i]][j] / src[index[j]][j];
            src[index[i]][j] = p;
            for (k = j + 1 ; k < n ; k++)
               src[index[i]][k] -= p * src[index[j]][k];
         }
      }

      for (i = 0 ; i < n - 1 ; i++)
      for (j = i + 1 ; j < n ; j++)
      for (k = 0 ; k < n ; k++)
         tmp[index[j]][k] -= src[index[j]][i] * tmp[index[i]][k];

      for (i = 0 ; i < n ; i++) {
         dst[n-1][i] = tmp[index[n-1]][i] / src[index[n-1]][n-1];
         for (j = n - 2 ; j >= 0 ; j--) {
            dst[j][i] = tmp[index[j]][i];
            for (k = j + 1 ; k < n ; k++)
               dst[j][i] -= src[index[j]][k] * dst[k][i];
            dst[j][i] /= src[index[j]][j];
         }
      }
   }
}