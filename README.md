# Corona Gel Totem

Aplicação em Java/Swing para um Totem sobre COVID-19 executado no Raspberry Pi 3B

## Raspbian

### Instalação do Raspbian

- Primeiro, baixe o ISO, do sistema operacional Raspbian que você instalará. Isso pode ser [baixado aqui](https://downloads.raspberrypi.org/raspbian_latest).

- Em seguida, você precisará criar um sdcard inicializável. No Windows, use o Rufus, que pode ser [baixado aqui](https://github.com/pbatard/rufus/releases/download/v3.10/rufus-3.10p.exe).

- Agora insira o SD Card no Raspberry Pi e execute o processo de instalação do Raspbian. Atualize o software quando pedir.

## Raspberry Pi 3B

### Configurações dos sensores

Pinos utilizados:

-   GPIO0: sensor de distância
-   GPIO2: atuador relé

<p align="center">
    <img src="https://github.com/kleberandrade/corona-gel/blob/master/figures/eletronic.png" width="750"/>
</p>

## Software

### Download do sistema

Abra o terminal (CTRL+ALT+T) e digite:

```bash
git clone https://github.com/kleberandrade/corona-gel
```

### Instalação do sistema

Abra o terminal (CTRL+ALT+T) e digite:

```bash
cd ~/corona-gel
chmod +x install.sh
./install.sh
```

### Atualiação do sistema (Opcional)

Abra o terminal (CTRL+ALT+T) e digite:

```bash
cd ~/corona-gel
chmod +x update.sh
./update.sh
```

### Colocando as imagens

*   **Desktop/CoronaGel/figures/sensors:** Nesta pasta sensors você deve colocar somente uma imagem (imagem que vai aparecer quando a pessoa colocar a mão para pegar o alcool em gel).
*   **Desktop/CoronaGel/figures/slides:** Na pasta slides você pode adicionar quantas imagens quiser, elas serão trocadas automaticamente.
