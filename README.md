# 🚀 AI Debugger

An AI-powered debugging assistant that uses **Retrieval-Augmented Generation (RAG)** to analyze logs, runbooks, and incidents, and generate contextual explanations for production issues.

## ✨ Key Capabilities
- Ingest logs, runbooks, and documentation  
- Generate semantic embeddings using **Cohere (embed-v4.0)**  
- Store and query vectors using **PostgreSQL (pgvector)**  
- Retrieve relevant context using similarity search  
- Generate explanations using **Hugging Face LLM (Llama 3.1 - 8B Instruct via Novita)**  
- Capture and manage production-like incidents  

---

## ⚙️ How It Works

1. Ingest logs/runbooks → stored in database  
2. Generate embeddings using Cohere  
3. Store vectors in PostgreSQL (pgvector)  
4. On query:
   - Perform vector similarity search  
   - Retrieve relevant context  
   - Send context to LLM (Hugging Face - Llama 3.1)  
5. Return AI-generated debugging insights  

---

## Architecture Layers

- controller   → REST API layer
- service      → Business logic + AI/RAG processing
- repository   → Data persistence layer
- entity       → JPA entities
- config       → System configuration

This structure ensures:
- Separation of concerns
- Maintainability
- Scalability for AI + microservice expansion


---

## 🚀 Getting Started

### Prerequisites
- Java `17+`  
- Maven `3.9+`  

### Run Locally
```bash
mvn spring-boot:run

Run Tests
mvn test
Local Endpoints
Debug API: http://localhost:8080/api
Incident API: http://localhost:8080/api/v1/incidents


🔍 Quick API Examples
Ingest Sample Data

curl -X POST "http://localhost:8080/api/ingest" \
  -H "Content-Type: application/json" \
  -d "{\"rawText\":\"Payment service failure: gateway timeout on checkout\"}"

Ask a Question (RAG + LLM)

Input:
curl -G "http://localhost:8080/api/ask" --data-urlencode "query=Why did payment service fail?"

Output (example):

{
    "answer": "**Root Cause:**\nThe payment service failed due to a NullPointerException at PaymentService.java line 45.\n\n**Explanation:**\nA NullPointerException occurs when the program attempts to use an object reference that has a null value. In this case, the error occurred at line 45 in the PaymentService.java file, indicating that an object was used without being initialized. This suggests that a variable or object was not assigned a value before it was used in the code.\n\n**Suggested Fix:**\nTo fix the NullPointerException, we need to identify the uninitialized object and initialize it before using it. Based on the Runbook, we will follow these steps:\n\n1. Review the code at line 45 in PaymentService.java to identify the object that was not initialized.\n2. Check the code above line 45 to see where the object is being used and where it should be initialized.\n3. Initialize the object before using it at line 45.\n\nFor example, if the object is a variable named \"payment\", we would add a line to initialize it before using it:\n```java\nPayment payment = new Payment(); // Initialize the payment object\n// Use the payment object at line 45\n```\nBy initializing the object before using it, we can prevent the NullPointerException and ensure that the payment service works correctly.",
    "context": [
        "ERROR NullPointerException at PaymentService.java line 45 due to uninitialized object",
        "Runbook: To fix NullPointerException, check for null values and initialize objects before use"
    ]
}


##🔗 External API Usage
Hugging Face (LLM)
curl -X POST "https://router.huggingface.co/v1/chat/completions" \
  -H "Authorization: Bearer <YOUR_HF_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "meta-llama/Llama-3.1-8B-Instruct:novita",
    "messages": [{"role": "user", "content": "What is the capital of France?"}],
    "stream": false
  }'
Cohere (Embeddings)
curl -X POST https://api.cohere.com/v2/embed \
  -H "Authorization: Bearer <YOUR_COHERE_API_KEY>" \
  -H "Content-Type: application/json" \
  -d '{
    "model": "embed-v4.0",
    "input_type": "classification",
    "texts": ["hello", "goodbye"],
    "embedding_types": ["float"]
  }'

##📚 API Endpoints
Incident APIs
  GET /api/v1/incidents → List incidents
  POST /api/v1/incidents → Create incident
  Debug APIs
  POST /api/ingest → Ingest logs/runbooks
  GET /api/ask?query=... → RAG + LLM response

##🧱 Tech Stack
  Spring Boot
  PostgreSQL + pgvector
  Cohere (Embeddings)
  Hugging Face (Llama 3.1 LLM)
  Docker

🛣️ Roadmap
  Add DTO layer for API decoupling
  Improve test coverage with mocked LLM responses
  Add database migrations (Flyway/Liquibase)
  Introduce agent-based debugging workflows
  Automated root cause analysis

