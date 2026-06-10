import os
import re

file_path = "app/build.gradle.kts"

with open(file_path, "r") as f:
    content = f.read()

version_code_match = re.search(r"versionCode\s*=\s*(\d+)", content)
version_name_match = re.search(r'versionName\s*=\s*"(\d+)\.(\d+)\.(\d+)"', content)

if version_code_match and version_name_match:
    old_code = int(version_code_match.group(1))
    new_code = old_code + 1
    
    major = int(version_name_match.group(1))
    minor = int(version_name_match.group(2))
    patch = int(version_name_match.group(3))
    new_patch = patch + 1
    new_name = f"{major}.{minor}.{new_patch}"
    
    # Replace in content
    content = re.sub(r"versionCode\s*=\s*\d+", f"versionCode = {new_code}", content)
    content = re.sub(r'versionName\s*=\s*"\d+\.\d+\.\d+"', f'versionName = "{new_name}"', content)
    
    with open(file_path, "w") as f:
        f.write(content)
        
    print(f"Bumped version from {old_code} ({major}.{minor}.{patch}) to {new_code} ({new_name})")
    
    if 'GITHUB_OUTPUT' in os.environ:
        with open(os.environ['GITHUB_OUTPUT'], 'a') as f:
            f.write(f"new_version={new_name}\n")
else:
    print("Could not find version info in app/build.gradle.kts")
    exit(1)
