import qwiic_buzzer
import time

def main():
    buzzer = qwiic_buzzer.QwiicBuzzer()

    if not buzzer.begin():
        print("Device did not connect! Freezing.")
        return

    print("Buzzer connected!")

    while True:
        buzzer.on()          # Turn buzzer on (default tone)
        time.sleep(1)        # Wait 1 second
        buzzer.off()         # Turn buzzer off
        time.sleep(1)        # Wait 1 second

if __name__ == '__main__':
    main()
