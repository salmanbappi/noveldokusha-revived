import os
import sys

def check_architecture(file_path):
    issues = []
    is_domain = "/domain/" in file_path
    is_data = "/data/" in file_path
    
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
            
        for i, line in enumerate(lines):
            line = line.strip()
            if not line.startswith("import "):
                continue
                
            imported = line.replace("import ", "").replace(";", "")
            
            # Domain Layer Rules
            if is_domain:
                if "android.view" in imported or "android.widget" in imported:
                    issues.append(f"❌ Domain importing Android View: {imported}")
                if "androidx.compose.ui" in imported:
                    issues.append(f"❌ Domain importing Compose UI: {imported}")
                if ".ui." in imported and not ".domain." in imported: # Heuristic
                    issues.append(f"⚠️ Domain importing UI layer: {imported}")
                    
            # Data Layer Rules
            if is_data:
                if ".ui." in imported:
                     issues.append(f"⚠️ Data importing UI layer: {imported}")

    except Exception:
        pass
        
    return issues

def main():
    root_dir = sys.argv[1] if len(sys.argv) > 1 else "."
    print(f"Checking architectural compliance in {root_dir}...")
    
    violation_count = 0
    
    for root, dirs, files in os.walk(root_dir):
        if "build" in root or "test" in root:
            continue
            
        for file in files:
            if file.endswith(".kt"):
                file_path = os.path.join(root, file)
                issues = check_architecture(file_path)
                
                if issues:
                    print(f"\n🏛️ {file_path}")
                    for issue in issues:
                        print(f"  {issue}")
                        violation_count += 1
                        
    if violation_count == 0:
        print("✅ Architecture looks clean.")
    else:
        print(f"\n🚨 Found {violation_count} architectural violations.")

if __name__ == "__main__":
    main()
