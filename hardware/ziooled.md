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

---

```python
import empyrebase
from luma.core.interface.serial import i2c
from luma.core.render import canvas
from luma.oled.device import ssd1327
from PIL import ImageFont
from time import sleep

serial = i2c(port=1, address=0x3C)
device = ssd1327(serial)
device.clear()

config = {
    "apiKey": "",
    "authDomain": "sitesync-ff98c.firebaseapp.com",
    "databaseURL": "https://sitesync-ff98c-default-rtdb.firebaseio.com",
    "storageBucket": "sitesync-ff98c.appspot.com",
    "projectId": "sitesync-ff98c"
}

data_font_size = 10
data_font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", data_font_size)

header_font_size = 12
header_font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", header_font_size)

firebase = empyrebase.initialize_app(config)
firestore_db = firebase.firestore()

COLLECTION_NAME = "Accounts"
DOCUMENT_ID = "0sVBDoz76Bb98QoU53aaHXaVPzf2"
DOCUMENT_PATH = f"/{COLLECTION_NAME}/{DOCUMENT_ID}"

HEADER_TEXT = "ACCOUNT DATA"
MAX_WIDTH = device.width

while True:
    try:
        doc_data_raw = firestore_db.get_document(DOCUMENT_PATH)
        account_data = doc_data_raw

        if account_data and isinstance(account_data, dict):
            with canvas(device) as draw:
                y_pos = 0
                data_line_height = 10 

                header_width = draw.textlength(HEADER_TEXT, font=header_font)
                header_x = (MAX_WIDTH - header_width) // 2
                draw.text((header_x, y_pos), HEADER_TEXT, fill="white", font=header_font)
                
                y_pos += header_font_size + 4 

                for key in sorted(account_data.keys()):
                    value = account_data[key] 
                    
                    if y_pos > (device.height - data_line_height):
                        draw.text((0, y_pos), "...", fill="white", font=data_font)
                        break
                        
                    cleaned_value = str(value).strip()
                    display_text = cleaned_value 
                    
                    text_width = draw.textlength(display_text, font=data_font)
                    text_x = (MAX_WIDTH - text_width) // 2
                    
                    draw.text((text_x, y_pos), display_text, fill="white", font=data_font)

                    y_pos += data_line_height

        else:
            device.clear()
            with canvas(device) as draw:
                draw.text((0, 0), "Data Error", fill="white", font=header_font)

    except Exception as e:
        device.clear()
        with canvas(device) as draw:
            draw.text((0, 0), f"Error: {e}", fill="white", font=data_font)

    sleep(5)
```

---

## Troubleshooting

[Generic](https://github.com/PrototypeZone/ceng317/blob/main/hardware/troubleshooting.md)  

- Make sure I2C is enabled on the Raspberry Pi.  
- Use `sudo i2cdetect -y 1` to confirm the ziooled address.  
- Check wiring between the sensor and the Pi if you get no readings.  
- Makesure when installing the python libraries, to use a venv `python3 -m venv venv` 

## Future work

[empyrebase integration](https://github.com/emrothenberg/empyrebase)

[Sensehat integration](https://github.com/astro-pi/python-sense-hat)



