# Build instructions for ziooled

![Photo](https://github.com/PrototypeZone/ceng317/blob/main/hardware/projects/media/ziooled.jpg)

The target audience is a CENG student from any post-secondary program who would like to recreate this project that measures temperature using the ziooled 1.5 inch oled display with a Raspberry Pi.

## Materials

[Order parts from Digikey](https://www.digikey.ca/short/hcqwtzcb)  
[Order parts list (GitHub)](https://github.com/PrototypeZone/ceng317/blob/main/hardware/digikeyorder.md)  
[Order PCB](https://github.com/PrototypeZone/ceng317/tree/main/hardware/pcb)  
[Order plastic case](https://github.com/PrototypeZone/ceng317/tree/main/hardware/lasercutting)  
[Soldering](https://github.com/PrototypeZone/ceng317/blob/main/hardware/pcb/inspection.md)  
[Raspberrypi Image](https://github.com/PrototypeZone/ceng153/blob/main/image.md)  

## Software

Required system packages:  

[I2C Tools](https://github.com/MLAB-project/i2c-tools)    
[Luma Oled](https://github.com/rm-hull/luma.oled)  
[Empyrebase](https://github.com/rm-hull/luma.oled)  
[Pillow](https://github.com/python-pillow/Pillow)  

- `sudo apt update`  
- `sudo apt install python3 python3-pip python3-smbus i2c-tools` 
- Enable I2C using `sudo raspi-config` → Interface Options → I2C 
- `sudo apt install python3-pip libjpeg-dev zlib1g-dev libfreetype6-dev liblcms2-dev`
- `pip3 install empyrebase`
- `pip3 install luma.oled`
- `pip3 install Pillow` 

[Python code](ziooled.py)  

## Troubleshooting

[Generic](https://github.com/PrototypeZone/ceng317/blob/main/hardware/troubleshooting.md)  

- Make sure I2C is enabled on the Raspberry Pi.  
- Use `sudo i2cdetect -y 1` to confirm the ziooled address.  
- Check wiring between the sensor and the Pi if you get no readings.  
- Makesure when installing the python libraries, to use a venv `python3 -m venv venv` 

## Future work

[empyrebase integration](https://github.com/emrothenberg/empyrebase)

[Sensehat integration](https://github.com/astro-pi/python-sense-hat)




