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
    "apiKey": "AIzaSyBMLFUN5RmpSzHM2IbeFphqtzANUjYGGjI",
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
