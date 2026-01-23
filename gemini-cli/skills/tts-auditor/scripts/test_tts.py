import sys
import requests
import json

def test_gemini_tts(api_key, text):
    # Using the correct structure for Gemini 2.5 TTS models
    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-tts:generateContent?key={api_key}"
    
    payload = {
        "contents": [{
            "parts": [{
                "text": text
            }]
        }],
        "generationConfig": {
            "response_modalities": ["AUDIO"]
        }
    }
    
    try:
        response = requests.post(url, json=payload, timeout=30)
        data = response.json()
        
        if "candidates" in data:
            parts = data["candidates"][0]["content"]["parts"]
            found_audio = False
            for part in parts:
                if "inlineData" in part and part["inlineData"]["mimeType"] == "audio/mp3":
                    print("✅ SUCCESS: Received audio data (inlineData:audio/mp3)")
                    found_audio = True
                    break
                elif "blob" in part:
                    print("✅ SUCCESS: Received audio data (blob)")
                    found_audio = True
                    break
            
            if not found_audio:
                print(f"❌ ERROR: No audio part found in response. Keys: {parts[0].keys() if parts else 'EMPTY'}")
                print(json.dumps(data, indent=2))
        else:
            print(f"❌ ERROR: API Response failed: {json.dumps(data, indent=2)}")
            
    except Exception as e:
        print(f"❌ ERROR: Test failed: {str(e)}")
    return False

if __name__ == "__main__":
    test_gemini_tts(sys.argv[1], "Testing the laboratory environment with audio modality.")