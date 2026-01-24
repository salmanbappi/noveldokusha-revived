import os
import sys
import re

def analyze_file(file_path):
    issues = []
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
            
        line_count = len(lines)
        if line_count > 500:
            issues.append(f"📦 Class too large ({line_count} lines) - Consider refactoring")

        func_pattern = re.compile(r'^\s*fun\s+')
        current_func_start = -1
        current_func_lines = 0
        
        for i, line in enumerate(lines):
            # Check indentation (Nesting)
            indent = len(line) - len(line.lstrip())
            if indent > 16: # Assuming 4 spaces per indent -> 4 levels = 16 spaces
                issues.append(f"↳ Deep nesting at line {i+1} ({indent} spaces)")

            # Simple function length check (heuristic)
            if func_pattern.match(line):
                if current_func_start != -1:
                    if current_func_lines > 50:
                        issues.append(f"📏 Function starting at {current_func_start} is too long ({current_func_lines} lines)")
                current_func_start = i + 1
                current_func_lines = 0
            elif current_func_start != -1:
                if line.strip() == "} ": # Very naive end check, but works for top level
                    current_func_lines += 1
                else:
                    current_func_lines += 1

    except Exception as e:
        pass # Skip unreadable files
        
    return issues

def main():
    root_dir = sys.argv[1] if len(sys.argv) > 1 else "."
    print(f"Scanning {root_dir} for code quality issues...")
    
    issue_count = 0
    
    for root, dirs, files in os.walk(root_dir):
        if "build" in root or ".git" in root or ".idea" in root:
            continue
            
        for file in files:
            if file.endswith(".kt") or file.endswith(".java"):
                file_path = os.path.join(root, file)
                file_issues = analyze_file(file_path)
                
                if file_issues:
                    print(f"\n📄 {file_path}")
                    for issue in file_issues:
                        print(f"  - {issue}")
                        issue_count += 1

    if issue_count == 0:
        print("✨ Clean code! No obvious complexity issues found.")
    else:
        print(f"\n⚠️  Found {issue_count} potential quality issues.")

if __name__ == "__main__":
    main()
