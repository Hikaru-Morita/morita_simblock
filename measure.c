#include <stdio.h>
#include <stdlib.h>

#define MAXLINE_SIZE 4*1001

int main(void){
  FILE *fp;
  // char *fname = "./simulator/src/dist/output/individual_bpt.csv";
  char *fname = "test.csv";
  int ret;
  char buf[3][3]; 
  double data[3];

  fp = fopen( fname, "r" );
  if( fp == NULL ){
    printf( "%sファイルが開けません\n", fname );
    return -1;
  }

  printf("\n");
  
  fscanf(fp, "%[^,],%[^,],%s", buf[0], buf[1], buf[2]);
  printf("%s %s %s\n",buf[0], buf[1], buf[2]);

  while( (ret=fscanf(fp, "%lf,%lf,%lf", &data[0], &data[1], &data[2])) != EOF){
    printf("%lf %lf %lf\n", data[0], data[1], data[2]);
  }

  printf("\n");
  fclose( fp );
}