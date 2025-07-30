# JournAI 📝✨

> AI-Powered Journaling & Mood Tracking Application

JournAI is a modern journaling application that combines the power of artificial intelligence with personal reflection to help users track their mood, gain insights, and improve their mental wellness through intelligent journaling.

## 🚀 What is JournAI?

JournAI is a full-stack application that helps you:
- Write and organize your daily thoughts and experiences
- Track your mood and emotional patterns over time
- Get AI-powered insights and reflection prompts
- Visualize your mental wellness journey through analytics
- Access your journal from anywhere with a beautiful, responsive interface

## 🏗️ Project Structure

This project consists of two main components:

```
JournAI/
├── client/          # Frontend (Next.js + React)
├── server/          # Backend (Java Spring Boot)
├── k8s-manifests/   # Kubernetes deployment files
└── docker-compose.yml
```

### Frontend (Client)
- **Technology**: Next.js 15 with React and TypeScript
- **Styling**: Tailwind CSS with custom components
- **Authentication**: Clerk for user management
- **AI Integration**: Google's Generative AI for intelligent features
- **Rich Text Editor**: TipTap editor for enhanced writing experience

### Backend (Server)
- **Technology**: Java 21 with Spring Boot 3.5
- **Database**: PostgreSQL for data persistence
- **API**: RESTful APIs for client-server communication
- **Build Tool**: Maven for dependency management

## 🛠️ Prerequisites

Before you start, make sure you have these installed on your computer:

- **Node.js** (version 18 or higher) - [Download here](https://nodejs.org/)
- **Java** (version 21 or higher) - [Download here](https://adoptium.net/)
- **Docker** (optional, for containerized deployment) - [Download here](https://docker.com/)
- **Git** - [Download here](https://git-scm.com/)

## 🚀 Quick Start

### Option 1: Using Docker (Recommended for beginners)

1. **Clone the repository**
   ```bash
   git clone https://github.com/amitamrutiya/Journ-AI.git
   cd JournAI
   ```

2. **Set up environment variables**
   ```bash
   # Copy the example environment files
   cp client/.env.local.example client/.env.local
   cp server/.env.example server/.env
   # Edit the .env.local and .env files with your API keys
   ```

3. **Start the application**
   ```bash
   docker-compose up --build
   ```

4. **Access the application**
   - Frontend: Open your browser and go to `http://localhost:3000`
   - Backend API: Available at `http://localhost:8000`

### Option 2: Manual Setup (For developers)

#### Starting the Backend (Server)

1. **Navigate to the server directory**
   ```bash
   cd server
   ```

2. **Set up environment variables**
   ```bash
   # Copy the example environment file
   cp .env.example .env
   # Edit the .env file with your database and API keys
   ```

3. **Run the Spring Boot application**
   ```bash
   ./mvnw spring-boot:run
   ```
   
   The server will start on `http://localhost:8000`

#### Starting the Frontend (Client)

1. **Open a new terminal and navigate to the client directory**
   ```bash
   cd client
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Set up environment variables**
   ```bash
   # Copy the example environment file
   cp .env.local.example .env.local
   # Edit the .env.local file with your Clerk API keys
   ```

4. **Start the development server**
   ```bash
   npm run dev
   ```
   
   The client will start on `http://localhost:3000`

## 📱 Features

### Core Functionality
- **Smart Journaling**: Write entries with a rich text editor
- **Mood Tracking**: Log and visualize your emotional patterns
- **AI Insights**: Get personalized reflection prompts and insights
- **Calendar View**: Navigate through your journal entries by date
- **Search & Filter**: Find specific entries quickly

### AI-Powered Features
- **Intelligent Prompts**: AI suggests writing prompts based on your mood
- **Mood Analysis**: Automatic mood detection from your writing
- **Pattern Recognition**: Identify trends in your emotional well-being
- **Personalized Insights**: Get tailored advice and reflections

## 🔐 Environment Variables

You'll need to set up environment variables for both the client and server:

### Client (.env.local)
```env
# Clerk Authentication
NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY=pk_test_...
CLERK_SECRET_KEY=sk_test_...
WEBHOOK_SECRET=whsec_...
```

### Server (.env)
```env
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/journai
DATABASE_USERNAME=your_db_username
DATABASE_PASSWORD=your_db_password

# Clerk Configuration
CLERK_SECRET_KEY=sk_test_...
CLERK_WEBHOOK_SECRET=whsec_...

# AI Configuration
GEMINI_API_KEY=your_google_ai_api_key
GEMINI_MODEL=gemini-1.5-flash

# Server Configuration
PORT=8000
SPRING_PROFILES_ACTIVE=development
```

### How to get API Keys:
- **Clerk Keys**: Sign up at [clerk.com](https://clerk.com) and create a new application
- **Gemini API Key**: Get it from [Google AI Studio](https://aistudio.google.com/app/apikey)
- **Database**: Set up PostgreSQL locally or use a cloud service like Supabase

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request


**Happy Journaling! 📝✨**

Built with ❤️ by [Amit Amrutiya](https://github.com/amitamrutiya)
