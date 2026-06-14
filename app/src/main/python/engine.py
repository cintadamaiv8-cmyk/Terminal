import sys
import io

def run(code):
    try:
        # Tangkap output print()
        old_stdout = sys.stdout
        sys.stdout = buffer = io.StringIO()

        # Jalankan kode Python
        exec(code, {})

        # Ambil output asli
        output = buffer.getvalue()

        # Restore stdout
        sys.stdout = old_stdout

        # Return hanya output print
        return output.strip() if output.strip() else "OK"

    except Exception as e:
        if hasattr(sys, '__stdout__'):
            sys.stdout = sys.__stdout__
        return str(e)
