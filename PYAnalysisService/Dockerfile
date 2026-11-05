FROM python:3.12-slim
# ffmpeg normalizes uploads (iPhone .mov/HEVC) and re-encodes the tracked video; OpenCV needs libgl and glib
RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg libgl1 libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /service
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY app ./app
RUN useradd --system --uid 1001 pwr && mkdir -p /data/videos && chown pwr /data/videos
USER pwr
WORKDIR /service/app
ENV PWR_REQUIRE_FFMPEG=true
EXPOSE 8000
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
