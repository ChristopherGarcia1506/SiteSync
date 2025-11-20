from luma.core.interface.serial import i2c
from luma.core.render import canvas
from luma.oled.device import ssd1327
from PIL import ImageFont
from time import sleep

serial = i2c(port=1, address=0x3C)

device = ssd1327(serial)

backgroundColor = "white"

font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 12)
 

while True:
    with canvas(device) as draw:
        draw.rectangle(device.bounding_box,fill=backgroundColor)

        draw.text((0, 0), "Tyler Meira", fill="black",font=font)
        draw.text((0, 20), "Hello World", fill="black",font=font)
        draw.rectangle(device.bounding_box, outline="black")

sleep(5)

