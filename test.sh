for para in `seq 1 10`; do
    sed -i -e "14c public static double para_ = $para;" ./simulator/src/main/java/SimBlock/node/Score.java
    sudo gradle simulator:run
done