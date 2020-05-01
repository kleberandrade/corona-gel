#!/bin/bash

echo "Install Java SDK 8"
sudo apt update
sudo apt install openjdk-8-jdk

echo "Disable screen sleep"
sudo chmod +777 /etc/lightdm/lightdm.conf
sudo echo -e "$(cat /etc/lightdm/lightdm.conf)\r\n\r\n[SeatDefaults]\r\nxserver-command=X -s 0 -dpms" > /etc/lightdm/lightdm.conf

echo "Copy system to Desktop"
cp -r ~/corona-gel/dist/. ~/Desktop/CoronaGel
mkdir -p ~/Desktop/CoronaGel/figures/sensors
mkdir -p ~/Desktop/CoronaGel/figures/slides

echo "Auto Start"
echo "java -jar ~/Desktop/CoronaGel/CoronaGel.jar" > ~/coronagel.sh
sudo chmod +x ~/coronagel.sh
sudo echo "bash ~/coronagel.sh" >> ~/.bashrc
mkdir -p ~/.config/lxsession/LXDE-pi
echo -e "$(cat /etc/xdg/lxsession/LXDE-pi/autostart)\r\n@lxterminal" > ~/.config/lxsession/LXDE-pi/autostart

echo "Finish"
