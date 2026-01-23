import asyncio
import edge_tts
import sys
import os

async def generate_tts(text, voice, output_path, rate="+0%"):
    communicate = edge_tts.Communicate(text, voice, rate=rate)
    await communicate.save(output_path)

if __name__ == "__main__":
    if len(sys.argv) < 4:
        print("Usage: python edge_narrator.py <text> <voice> <output_path> [rate]")
        sys.exit(1)
        
    text = sys.argv[1]
    voice = sys.argv[2] # e.g. "en-US-AvaNeural"
    output_path = sys.argv[3]
    rate = sys.argv[4] if len(sys.argv) > 4 else "+0%"
    
    asyncio.run(generate_tts(text, voice, output_path, rate))
    print(f"SUCCESS: {output_path}")
