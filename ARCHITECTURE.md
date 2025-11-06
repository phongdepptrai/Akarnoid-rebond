# 🏗️ Kiến Trúc 4 Threads - Arkanoid Game

## 📐 Tổng Quan Kiến Trúc

```
┌─────────────────────────────────────────────────────────────────┐
│              Main Thread (UI/EDT)                               │
│              - Vẽ graphics (render())                           │
│              - Nhận input từ bàn phím (InputManager)            │
└────────────────────────┬────────────────────────────────────────┘
                         │
          ┌──────────────┼──────────────┬────────────────────┐
          │              │              │                    │
          ▼              ▼              ▼                    ▼
┌─────────────────┐ ┌────────────┐ ┌──────────────┐ ┌──────────────┐
│ Game Loop Thread│ │Audio Thread│ │  I/O Thread  │ │              │
│   (60 FPS)      │ │    Pool    │ │              │ │              │
├─────────────────┤ ├────────────┤ ├──────────────┤ │              │
│ ✓ Update Ball   │ │✓ Play sound│ │✓ Save game   │ │              │
│ ✓ Update Paddle │ │  effects   │ │✓ Load game   │ │              │
│ ✓ Check va chạm │ │✓ Background│ │✓ Load        │ │              │
│ ✓ Update điểm   │ │  music     │ │  resources   │ │              │
│ ✓ Update lives  │ │            │ │              │ │              │
└─────────────────┘ └────────────┘ └──────────────┘ └──────────────┘
```

## 🔧 Chi Tiết Từng Thread

### 1️⃣ Main Thread (AWT Event Dispatch Thread)
**Vai trò**: UI Thread - Xử lý rendering và input

**Chức năng**:
- Vẽ tất cả graphics qua `Graphics2D`
- Nhận input từ keyboard/mouse
- Không được block bởi I/O hoặc heavy operations

**Implementation**:
```java
// GameLoop.java - render() method
private void render() {
    BufferStrategy strategy = window.getCanvas().getBufferStrategy();
    Graphics2D graphics = (Graphics2D) strategy.getDrawGraphics();
    graphics.clearRect(0, 0, config.width(), config.height());
    scenes.render(graphics); // ← Chạy trên Main Thread
}
```

---

### 2️⃣ Game Loop Thread (60 FPS)
**Vai trò**: Game Logic Thread - Update game state

**Chức năng**:
- Update vị trí Ball, Paddle (60 lần/giây)
- Check va chạm (ball-paddle, ball-brick, ball-wall)
- Update score, lives, level progression
- Quản lý game state (paused, gameOver, etc.)

**Implementation**:
```java
// GameLoop.java - run() method
@Override
public void run() {
    final double targetFrameTime = 1_000_000_000.0 / 60; // 60 FPS
    while (running) {
        double deltaSeconds = targetFrameTime / 1_000_000_000.0;
        scenes.update(deltaSeconds); // ← Chạy trên Game Loop Thread
        render();
    }
}

// GameplayScene.java - update() method
@Override
public void update(double deltaTime) {
    ball.update(deltaTime);           // ← Update position
    paddle.update(deltaTime);
    constrainBallWithinArena();       // ← Check collisions
    handlePaddleCollision();
    handleBrickCollisions();
    objectiveEngine.update(deltaTime); // ← Update objectives
}
```

---

### 3️⃣ Audio Thread Pool
**Vai trò**: Async Audio Playback - Sound effects & Background music

**Chức năng**:
- Play sound effects (brick hit, paddle hit, power-up)
- Loop background music
- Stop/pause audio
- **4 worker threads** để xử lý multiple sounds đồng thời

**Implementation**:
```java
// SoundManager.java
public class SoundManager {
    private final ExecutorService audioThreadPool;
    
    public SoundManager() {
        // Tạo Audio Thread Pool với 4 threads
        this.audioThreadPool = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "audio-thread");
            t.setDaemon(true);
            return t;
        });
    }
    
    public void play(String id) {
        audioThreadPool.submit(() -> {  // ← Async execution
            Clip clip = clips.get(id);
            synchronized (clip) {
                clip.setFramePosition(0);
                clip.start();
            }
        });
    }
    
    public void loop(String id) {
        audioThreadPool.submit(() -> {  // ← Async execution
            Clip clip = clips.get(id);
            synchronized (clip) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        });
    }
}
```

**Ví dụ sử dụng**:
```java
// Khi brick bị phá hủy
soundManager.play("brick-break");  // ← Non-blocking, trả về ngay lập tức

// Play background music
soundManager.loop("background-music");
```

---

### 4️⃣ I/O Thread
**Vai trò**: Async File Operations - Save/Load & Resource loading

**Chức năng**:
- Save game profile (async, không block game loop)
- Load game profile
- Load image resources (paddle, bricks, backgrounds)
- **Single thread** để đảm bảo thread-safe cho file operations

**Implementation**:
```java
// IOThreadPool.java - Singleton Pattern
public class IOThreadPool {
    private static IOThreadPool instance;
    private final ExecutorService ioThread;
    
    private IOThreadPool() {
        this.ioThread = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "io-thread");
            t.setDaemon(true);
            return t;
        });
    }
    
    public Future<?> submit(Runnable task) {
        return ioThread.submit(task);
    }
}

// ProfileManager.java - Async save
public void saveProfile() {
    IOThreadPool.getInstance().submit(() -> {
        storage.save(activeProfile);  // ← Chạy trên I/O Thread
    });
}

// AssetManager.java - Async load
public Future<?> loadImageAsync(String id, String resourcePath) {
    return IOThreadPool.getInstance().submit(() -> {
        BufferedImage image = ImageIO.read(url);
        images.put(id, image);
    });
}
```

**Ví dụ sử dụng**:
```java
// Save profile không block game
profileManager.saveProfile();  // ← Returns immediately
game.continue();               // ← Game continues smoothly

// Load image async
assetManager.loadImageAsync("paddle", "/graphics/paddle.PNG");
```

---

## 🔐 Thread Safety

### Synchronization Points

**1. Audio Clips**:
```java
synchronized (clip) {
    clip.setFramePosition(0);
    clip.start();
}
```

**2. Image Assets**:
```java
public BufferedImage getImage(String id) {
    synchronized (images) {
        return images.get(id);
    }
}
```

**3. Profile Save**:
```java
// Synchronous save on shutdown để đảm bảo data integrity
public void stop() {
    profileManager.saveProfileSync();  // ← Block until saved
    loop.stop();
    soundManager.dispose();
    IOThreadPool.getInstance().shutdown();
}
```

---

## 📊 Performance Benefits

### Before (2 Threads):
```
Main Thread: Render + Audio + Save/Load (BLOCKING) ❌
    └── Lag khi play sound
    └── Freeze khi save profile
    └── Stutter khi load images

Game Loop Thread: Update + Collision ✅
```

### After (4 Threads):
```
Main Thread: Render ONLY ✅
    └── Smooth 60 FPS

Game Loop Thread: Update + Collision ✅
    └── No blocking

Audio Thread Pool: Sound effects + Music ✅
    └── 4 concurrent sounds
    └── No game lag

I/O Thread: Save/Load + Resources ✅
    └── Background saving
    └── No game freeze
```

---

## 🎯 Design Patterns Used

### 1. **Singleton Pattern**
- `IOThreadPool.getInstance()`
- `GameplayPanelRenderer.getInstance()`

### 2. **Factory Method Pattern**
- Audio thread creation
- I/O thread creation
- Game entity creation

### 3. **Thread Pool Pattern**
- Audio Thread Pool (4 workers)
- I/O Thread Pool (1 worker)

### 4. **Observer Pattern**
- `ObjectiveEngine.Listener`
- Input event handling

---

## 🚀 Sử Dụng

### Compile & Run:
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.arcade.arkanoid.ArcadeLauncher"
```

### Monitor Threads:
```bash
jconsole  # Attach to Java process
# → Threads tab → See all 4 threads running
```

---

## 📝 Notes

- **Daemon Threads**: Audio và I/O threads đều là daemon → tự động shutdown khi game exit
- **Graceful Shutdown**: Profile được save synchronously trước khi exit
- **Thread Names**: 
  - `"game-loop"` - Game Loop Thread
  - `"audio-thread"` - Audio Thread Pool workers
  - `"io-thread"` - I/O Thread
- **FPS Target**: 60 FPS với fixed timestep

---

## ✅ Checklist Implementation

- [x] Main Thread (UI/EDT) - Render & Input
- [x] Game Loop Thread (60 FPS) - Update & Collision
- [x] Audio Thread Pool - Async sound playback
- [x] I/O Thread - Async save/load
- [x] Thread-safe synchronization
- [x] Graceful shutdown
- [x] Singleton patterns
- [x] Factory methods
- [x] Performance optimization

**BUILD SUCCESS** ✅
