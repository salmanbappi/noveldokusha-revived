import os
import re
import shutil

mainDir = os.getcwd()
workDir = os.path.join(mainDir, "app", "build", "outputs", "apk")

extension = ".apk"

def setEnvValue(key, value):
    print(f"Setting env varaible: {key}={value}")
    with open(os.environ['GITHUB_ENV'], 'a') as f:
        f.write(f"{key}={value}\n")

def getAPKs():
    list_apks = []
    for root, dirs, files in os.walk(workDir):
        for file in files:
            if file.endswith(extension) and "unsigned" not in file.lower():
                list_apks.append([root, file])
    return list_apks

def processAPK(path, fileName):
    fileNamePath = os.path.join(path, fileName)
    
    # Try to match flavor from path if filename match fails
    # Paths are usually: app/build/outputs/apk/<flavor>/<buildType>
    path_parts = path.replace("\\\\", "/").split("/")
    flavour = "unknown"
    if "full" in path_parts:
        flavour = "full"
    elif "foss" in path_parts:
        flavour = "foss"
    
    # Try to extract version from filename
    # NovelDokusha_v2.4.59-full-release.apk
    version_match = re.search(r"v(\d+\.\d+\.\d+)", fileName)
    if version_match:
        version = version_match.group(1)
    else:
        # Fallback to env or default
        version = os.environ.get("GITHUB_REF_NAME", "0.0.0").replace("v", "")

    newFileName = f"NovelDokusha_v{version}_{flavour}.apk"
    newFileNamePath = os.path.join(mainDir, newFileName)

    shutil.copy2(fileNamePath, newFileNamePath)

    print(f"Processed: {fileName} -> {newFileName} (version={version}, flavor={flavour})")

    setEnvValue("APP_VERSION", version)
    setEnvValue(f"APK_FILE_PATH_{flavour}", newFileName)

apks = getAPKs()
if not apks:
    print("No APKs found!")
    exit(1)

for [path, fileName] in apks:
    processAPK(path, fileName)