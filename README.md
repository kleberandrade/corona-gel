# Corona Gel Totem

Aplicação em Java/Swing para um Totem sobre COVID-19 executado no Raspberry Pi 3B

## Raspbian

### Instalação

Primeiro, baixe o ISO, a imagem do sistema operacional Raspbian que você instalará. Isso pode ser [baixado aqui](https://downloads.raspberrypi.org/raspbian_latest).

Em seguida, você precisará criar um pendrive inicializável. No Windows, um bom programa para criar um pendrive inicializável é chamado Rufus, que pode ser [baixado aqui](https://github.com/pbatard/rufus/releases/download/v3.10/rufus-3.10p.exe).

Pegue um SD card classe 10, com um tamanho de 2 GB ou superior e execute o arquivo .exe do Rufus baixado. No Rufus, clique no menu suspenso do dispositivo para selecionar o pendrive. Em seguida, clique no ícone que mostra uma imagem de uma bandeja de CD / DVD, que fica a cerca de três quartos da caixa, ao lado de 'Criar um disco inicializável usando' e selecione o arquivo ISO do Raspbian que você acabou de baixar. Depois clique em Iniciar.

Agora insira o SD Card no Raspberry Pi e execute o processo de instalação.

### Manter a tela sempre ativa

Para manter a tela sempre ativa, é necessário abrir o terminal (CTRL + ALT + T) e digitar o seguinte comando para abri o arquivo lightdm.conf

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

## Software

Texto ...

### Localização das imagens

Texto ...

### Auto inicialização do software

Texto ...

### Girar o desktop

Texto ...

## Raspberry Pi 3B

Texto ...

### Configurações dos sensores

Texto ...
