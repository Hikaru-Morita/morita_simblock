for num in `seq 100`; do
		# sudo /home/hikaru-morita/simblock/original/Simblock/gradle simulator:run
		sudo gradle simulator:run
		# python3 /home/hikaru-morita/デスクトップ/readcsv.py
		echo num:$num
	done