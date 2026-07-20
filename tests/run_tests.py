import subprocess
import sys
import os


def run_all_tests():
    cmd = [
        sys.executable, "-m", "pytest",
        "--html=reports/test_report.html",
        "--cov=.",
        "--cov-report=html:reports/coverage",
        "--cov-report=term",
        "-v"
    ]
    subprocess.run(cmd, check=True)


def run_tests_by_mark(mark):
    cmd = [
        sys.executable, "-m", "pytest",
        f"-m", mark,
        "--html=reports/test_report_{mark}.html",
        "-v"
    ]
    subprocess.run(cmd, check=True)


def run_test_file(file_path):
    cmd = [
        sys.executable, "-m", "pytest",
        file_path,
        "--html=reports/test_report.html",
        "-v"
    ]
    subprocess.run(cmd, check=True)


if __name__ == "__main__":
    os.makedirs("reports", exist_ok=True)
    
    if len(sys.argv) > 1:
        arg = sys.argv[1]
        
        if arg.startswith("-m") and len(sys.argv) > 2:
            run_tests_by_mark(sys.argv[2])
        elif arg in ["-a", "--all"]:
            run_all_tests()
        elif arg.endswith(".py"):
            run_test_file(arg)
        else:
            run_tests_by_mark(arg)
    else:
        run_all_tests()
