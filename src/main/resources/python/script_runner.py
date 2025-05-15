import sys
import subprocess
import argparse

def main():
    parser = argparse.ArgumentParser(description="Run Python script with conda environment")
    parser.add_argument("--env", required=True, help="Conda environment name")
    parser.add_argument("--script", required=True, help="Path to Python script")
    parser.add_argument("--args", help="Arguments for the script", nargs=argparse.REMAINDER)
    
    args = parser.parse_args()
    
    cmd = ["conda", "run", "-n", args.env, "python", "-u", args.script]
    if args.args:
        cmd.extend(args.args)
    
    process = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    stdout, stderr = process.communicate()
    
    print(stdout)
    if stderr:
        print("ERROR:", stderr, file=sys.stderr)
    
    return process.returncode

if __name__ == "__main__":
    sys.exit(main())