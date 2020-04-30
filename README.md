# Corona Gel Totem

Aplicação em Java/Swing para um Totem sobre COVID-19 executado no Raspberry Pi 3B

## Raspbian

### Instalação

Primeiro, baixe o ISO, a imagem do sistema operacional Raspbian que você instalará. Isso pode ser [baixado aqui](https://downloads.raspberrypi.org/raspbian_latest).

Em seguida, você precisará criar um pendrive inicializável. No Windows, um bom programa para criar um pendrive inicializável é chamado Rufus, que pode ser [baixado aqui](https://github.com/pbatard/rufus/releases/download/v3.10/rufus-3.10p.exe).

Pegue um SD card classe 10, com um tamanho de 2 GB ou superior e execute o arquivo .exe do Rufus baixado. No Rufus, clique no menu suspenso do dispositivo para selecionar o pendrive. Em seguida, clique no ícone que mostra uma imagem de uma bandeja de CD / DVD, que fica a cerca de três quartos da caixa, ao lado de 'Criar um disco inicializável usando' e selecione o arquivo ISO do Raspbian que você acabou de baixar. Depois clique em Iniciar.

Agora insira o SD Card no Raspberry Pi e execute o processo de instalação.

### Manter a tela sempre ativa

Para manter a tela sempre ativa, é necessário abrir o terminal (CTRL+ALT+T) e digitar o seguinte comando para abri o arquivo **lightdm.conf**

```bash
sudo nano /etc/lightdm/lightdm.conf
```

Com o arquivo aberto, procure esta linha

```bash
# xserver-command=X
```

E altere a linha para ficar desta forma

```bash
xserver-command=X -s 0 -dpms
```

Salve o arquivo usando CTRL+X - S - ENTER. Logo após, reinicie.

```bash
sudo reboot
```

### Rotacionar a tela

Abra o terminal (CTRL+ALT+T) e digite o seguinte comando para abri o arquivo **config.txt**

```bash
sudo nano /boot/config.txt
```

Adicione uma das seguintes linhas na parte inferior do arquivo:

```bash
display_rotate = 0
display_rotate = 1
display_rotate = 2
display_rotate = 3
```

0 é a configuração normal. 1 é 90 graus. 2 é 180 graus. 3 é 270 graus.

Salve o arquivo usando CTRL+X - S - ENTER. Logo após, reinicie.

```bash
sudo reboot
```

## Raspberry Pi 3B

### Pinagem do Raspberry utilizando a biblioteca Pi4j

<p align="center">
    <img src="https://pi4j.com/1.2/images/j8header-3b-plus-large.png" width="300"/>
</p>

### Configurações dos sensores

Pinos utilizados:

-   GPIO15: saída para o relé
-   GPIO16: entrada do sensor de distância (infravermelho)

<p align="center">
    <img src="https://github.com/kleberandrade/corona-gel/blob/master/figures/eletronic.png" width="600"/>
</p>

## Software

### Localização das imagens

Adicione todas as imagens que serão utilizadas em:

```bash
/home/pi/Pictures/
```

### Auto inicialização do software

Para colocar o software para inicializar automaticamente, abra o terminal (CTRL+ALT+T)

```bash
sudo nano ~/myjar.sh
```

Dentro do arquivo digite:

```bash
java -jar /home/pi/Desktop/CoronaGel.jar
```

Salve o arquivo usando CTRL+X - S - ENTER. Ainda no terminal, vamos alterar a permissão do **myjar.sh**

```bash
sudo chmod ugo+x ~/myhar.sh
```

Agora vamos abrir o arquivo **.bashrc**, para isso digite:

```bash
sudo nano ~/.bashrc
```

Ao abrir o arquivo, navega até a ultima linha e digite

```bash
bash myjar.sh
```

Salve o arquivo usando CTRL+X - S - ENTER. Ainda no terminar, digite

```bash
sudo nano /etc/xdg/lxsession/LXDE-pi/autostart
```

No final do arquivo digite

```bash
@lxterminal
```

Salve o arquivo usando CTRL+X - S - ENTER. Em seguida, reinicie

```bash
sudo reboot
```
