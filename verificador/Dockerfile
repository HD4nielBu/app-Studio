FROM python:3.12-slim
RUN pip install --no-cache-dir sympy==1.14.0
WORKDIR /app
COPY app.py test_app.py ./
USER nobody
EXPOSE 8000
CMD ["python", "app.py"]
