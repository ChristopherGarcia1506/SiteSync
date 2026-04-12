import qwiic_buzzer
import time
import firebase_admin
from firebase_admin import credentials, firestore

def main():
    # Initialize buzzer
    buzzer = qwiic_buzzer.QwiicBuzzer()
    if not buzzer.begin():
        print("Device did not connect! Freezing.")
        return
    print("Buzzer connected!")

    # Initialize Firebase
    cred = credentials.Certificate("servicekey.json")
    firebase_admin.initialize_app(cred)
    db = firestore.client()

    doc_ref = db.collection("Devices").document("Buzzer1")

    print("Listening for Firebase changes...")

    # Real-time listener
    def on_snapshot(doc_snapshot, changes, read_time):
        for doc in doc_snapshot:
            data = doc.to_dict()
            state = data.get("buzzer", False)

            if state:
                print("Firebase says: BUZZER ON")
                buzzer.on()
                time.sleep(1)        # Wait 1 second
                buzzer.off()         # Turn buzzer off

            else:
                print("Firebase says: BUZZER OFF")
                buzzer.off()

    doc_ref.on_snapshot(on_snapshot)

    # Keep script alive
    while True:
        time.sleep(1)

if __name__ == '__main__':
    main()
