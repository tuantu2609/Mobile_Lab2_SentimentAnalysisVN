from flask import Flask, request, jsonify
from flask_cors import CORS
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch

# Load model và tokenizer từ thư mục đã lưu
model_path = "./phobert_sentiment_model"
tokenizer = AutoTokenizer.from_pretrained(model_path)
model = AutoModelForSequenceClassification.from_pretrained(model_path)

# Khởi tạo Flask app
app = Flask(__name__)
CORS(app)  # Cho phép gọi API từ frontend

# API endpoint
@app.route('/predict', methods=['POST'])
def predict():
    try:
        # Nhận dữ liệu JSON từ client
        data = request.get_json()
        text = data['text']

        # Tokenize và dự đoán
        inputs = tokenizer(text, return_tensors="pt", truncation=True)
        with torch.no_grad():
            outputs = model(**inputs)
            predicted_class = torch.argmax(outputs.logits, dim=1).item()

        # Ánh xạ nhãn sang cảm xúc (kiểm tra lại model.config.id2label)
        label_mapping = model.config.id2label
        sentiment = label_mapping.get(predicted_class, "Unknown")

        return jsonify({
            "text": text,
            "sentiment": sentiment,
            "label": predicted_class
        })

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)