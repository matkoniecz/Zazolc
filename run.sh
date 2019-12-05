cd /home/mateusz/Documents/Archiwum/StreetComplete-ngi-zero
git add --all
git commit -m "thrown away"
git reset --hard @~
git switch master
git pull upstream-westnordost master
cd /home/mateusz/Desktop/streetcomplete_licence_chore
java -jar out.jar > licence_data.rb
ruby adder.rb
cd /home/mateusz/Documents/Archiwum/StreetComplete-ngi-zero
reuse download --all 
reuse lint