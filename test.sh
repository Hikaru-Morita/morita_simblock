#
for num in {1..9}; do 

	for i in {0..5}; do
		sudo gradle simulator:run
	done

	python3 addlineBFT.py $num
done