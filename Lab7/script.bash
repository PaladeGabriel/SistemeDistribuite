#!/bin/bash

# Numele fișierului jar pentru bidder
JAR_FILE="SD_Laborator_07/Okazii/out/artifacts/BidderMicroservice_jar/BidderMicroservice.jar"

# Lansează 100 de bidderi în paralel
for i in {1..100}
do
    # Lansează fiecare bidder într-un proces de fundal
    echo "Lansând Bidder $i..."
    java -jar $JAR_FILE &

    
    sleep 0.1  # Pauză de 0.1 secunde între instanțe, ajustabilă
done

# Așteaptă ca toate instanțele de bidder să termine
wait
echo "Toți bidderii au fost lansați."

