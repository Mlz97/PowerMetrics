import os


def analyzed_output_path(video_path: str) -> str:
    """Path for the annotated video, always distinct from the input regardless of its extension."""
    base, _ = os.path.splitext(video_path)
    return f"{base}_analyzed.mp4"
