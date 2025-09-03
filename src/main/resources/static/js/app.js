// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 初始化所有功能
    initAnimations();
    initInteractiveElements();
    initFormValidation();
    initVideoPlayer();
    initScrollEffects();
    initTooltips();
});

// 动画初始化
function initAnimations() {
    // 为页面元素添加渐入动画
    const elements = document.querySelectorAll('.card, .btn, .alert');
    elements.forEach((element, index) => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(30px)';
        
        setTimeout(() => {
            element.style.transition = 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)';
            element.style.opacity = '1';
            element.style.transform = 'translateY(0)';
        }, index * 100);
    });

    // 导航栏滚动效果
    let lastScrollTop = 0;
    const navbar = document.querySelector('.navbar');
    
    window.addEventListener('scroll', function() {
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        
        if (scrollTop > lastScrollTop && scrollTop > 100) {
            // 向下滚动，隐藏导航栏
            navbar.style.transform = 'translateY(-100%)';
        } else {
            // 向上滚动，显示导航栏
            navbar.style.transform = 'translateY(0)';
        }
        
        // 添加背景模糊效果
        if (scrollTop > 50) {
            navbar.style.background = 'rgba(255, 255, 255, 0.95)';
            navbar.style.backdropFilter = 'blur(20px)';
        } else {
            navbar.style.background = 'rgba(255, 255, 255, 0.9)';
        }
        
        lastScrollTop = scrollTop;
    });
}

// 交互元素初始化
function initInteractiveElements() {
    // 搜索框动态效果
    const searchInput = document.querySelector('.search-form input');
    if (searchInput) {
        searchInput.addEventListener('focus', function() {
            this.parentElement.style.transform = 'scale(1.02)';
        });
        
        searchInput.addEventListener('blur', function() {
            this.parentElement.style.transform = 'scale(1)';
        });
    }

    // 卡片悬停效果增强
    const cards = document.querySelectorAll('.card');
    cards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-10px) scale(1.02)';
            this.style.boxShadow = '0 20px 60px rgba(0, 0, 0, 0.15)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
            this.style.boxShadow = '0 10px 30px rgba(0, 0, 0, 0.1)';
        });
    });

    // 按钮点击效果
    const buttons = document.querySelectorAll('.btn');
    buttons.forEach(button => {
        button.addEventListener('click', function(e) {
            // 创建波纹效果
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;
            
            ripple.style.width = ripple.style.height = size + 'px';
            ripple.style.left = x + 'px';
            ripple.style.top = y + 'px';
            ripple.style.position = 'absolute';
            ripple.style.borderRadius = '50%';
            ripple.style.background = 'rgba(255, 255, 255, 0.5)';
            ripple.style.transform = 'scale(0)';
            ripple.style.animation = 'ripple 0.6s linear';
            ripple.style.pointerEvents = 'none';
            
            this.appendChild(ripple);
            
            setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    });

    // 视频卡片特殊效果
    const videoCards = document.querySelectorAll('.video-card');
    videoCards.forEach(card => {
        const thumbnail = card.querySelector('.thumbnail');
        
        card.addEventListener('mouseenter', function() {
            if (thumbnail) {
                thumbnail.style.transform = 'scale(1.1)';
                thumbnail.style.filter = 'brightness(1.1)';
            }
        });
        
        card.addEventListener('mouseleave', function() {
            if (thumbnail) {
                thumbnail.style.transform = 'scale(1)';
                thumbnail.style.filter = 'brightness(1)';
            }
        });
    });
}

// 表单验证
function initFormValidation() {
    const forms = document.querySelectorAll('form');
    
    forms.forEach(form => {
        const inputs = form.querySelectorAll('input, textarea, select');
        
        inputs.forEach(input => {
            // 实时验证
            input.addEventListener('input', function() {
                validateInput(this);
            });
            
            // 失焦验证
            input.addEventListener('blur', function() {
                validateInput(this);
            });
            
            // 聚焦效果
            input.addEventListener('focus', function() {
                this.style.transform = 'scale(1.02)';
                this.style.borderColor = 'var(--primary-color)';
            });
            
            input.addEventListener('blur', function() {
                this.style.transform = 'scale(1)';
                if (!this.value) {
                    this.style.borderColor = '#e9ecef';
                }
            });
        });
        
        // 表单提交处理
        form.addEventListener('submit', function(e) {
            let isValid = true;
            
            inputs.forEach(input => {
                if (!validateInput(input)) {
                    isValid = false;
                }
            });
            
            if (!isValid) {
                e.preventDefault();
                showNotification('请检查表单中的错误', 'error');
            }
            // 移除了搜索表单的加载状态逻辑
        });
    });
}

// 输入验证函数
function validateInput(input) {
    const value = input.value.trim();
    const type = input.type;
    const required = input.hasAttribute('required');
    let isValid = true;
    let message = '';
    
    // 移除之前的错误样式
    input.classList.remove('is-invalid', 'is-valid');
    const existingFeedback = input.parentElement.querySelector('.invalid-feedback');
    if (existingFeedback) {
        existingFeedback.remove();
    }
    
    // 必填验证
    if (required && !value) {
        isValid = false;
        message = '此字段为必填项';
    }
    
    // 类型验证
    if (value && type === 'email') {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(value)) {
            isValid = false;
            message = '请输入有效的邮箱地址';
        }
    }
    
    if (value && type === 'password') {
        if (value.length < 6) {
            isValid = false;
            message = '密码至少需要6个字符';
        }
    }
    
    // 文件验证
    if (type === 'file' && input.files.length > 0) {
        const file = input.files[0];
        const maxSize = 200 * 1024 * 1024; // 200MB
        
        if (file.size > maxSize) {
            isValid = false;
            message = '文件大小不能超过200MB';
        }
        
        if (input.accept && input.accept.includes('video/')) {
            const validTypes = ['video/mp4', 'video/avi', 'video/mov', 'video/wmv'];
            if (!validTypes.includes(file.type)) {
                isValid = false;
                message = '请选择有效的视频文件格式';
            }
        }
    }
    
    // 应用验证结果
    if (isValid && value) {
        input.classList.add('is-valid');
    } else if (!isValid) {
        input.classList.add('is-invalid');
        
        // 添加错误信息
        const feedback = document.createElement('div');
        feedback.className = 'invalid-feedback';
        feedback.textContent = message;
        input.parentElement.appendChild(feedback);
    }
    
    return isValid;
}

// 视频播放器增强
function initVideoPlayer() {
    const videos = document.querySelectorAll('video');
    
    videos.forEach(video => {
        // 添加播放控制
        video.addEventListener('loadstart', function() {
            showLoadingSpinner(this);
        });
        
        video.addEventListener('canplay', function() {
            hideLoadingSpinner(this);
        });
        
        video.addEventListener('play', function() {
            // 暂停其他视频
            videos.forEach(otherVideo => {
                if (otherVideo !== this) {
                    otherVideo.pause();
                }
            });
        });
        
        // 添加全屏按钮
        addFullscreenButton(video);
    });
}

// 滚动效果
function initScrollEffects() {
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('fade-in');
                
                // 计数动画
                if (entry.target.classList.contains('stats-number')) {
                    animateNumber(entry.target);
                }
            }
        });
    }, {
        threshold: 0.1
    });
    
    // 观察所有卡片和统计数字
    const elements = document.querySelectorAll('.card, .stats-number');
    elements.forEach(element => {
        observer.observe(element);
    });
}

// 工具提示初始化
function initTooltips() {
    // 创建自定义tooltip
    const elementsWithTitle = document.querySelectorAll('[title]');
    
    elementsWithTitle.forEach(element => {
        const title = element.getAttribute('title');
        element.removeAttribute('title');
        
        element.addEventListener('mouseenter', function(e) {
            showTooltip(e.target, title);
        });
        
        element.addEventListener('mouseleave', function() {
            hideTooltip();
        });
    });
}

// 工具函数
function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `alert alert-${type} notification`;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        z-index: 9999;
        min-width: 300px;
        transform: translateX(100%);
        transition: transform 0.3s ease;
    `;
    notification.innerHTML = `
        <span>${message}</span>
        <button type="button" class="btn-close" onclick="this.parentElement.remove()"></button>
    `;
    
    document.body.appendChild(notification);
    
    // 滑入动画
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
    }, 100);
    
    // 自动移除
    setTimeout(() => {
        notification.style.transform = 'translateX(100%)';
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 5000);
}

function showLoadingSpinner(element) {
    const spinner = document.createElement('div');
    spinner.className = 'loading-overlay';
    spinner.style.cssText = `
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        z-index: 10;
    `;
    spinner.innerHTML = '<div class="loading-spinner"></div>';
    
    element.parentElement.style.position = 'relative';
    element.parentElement.appendChild(spinner);
}

function hideLoadingSpinner(element) {
    const spinner = element.parentElement.querySelector('.loading-overlay');
    if (spinner) {
        spinner.remove();
    }
}

function addFullscreenButton(video) {
    const button = document.createElement('button');
    button.className = 'btn btn-sm btn-dark fullscreen-btn';
    button.style.cssText = `
        position: absolute;
        top: 10px;
        right: 10px;
        opacity: 0;
        transition: opacity 0.3s ease;
    `;
    button.innerHTML = '<i class="bi bi-fullscreen"></i>';
    
    button.addEventListener('click', function() {
        if (video.requestFullscreen) {
            video.requestFullscreen();
        }
    });
    
    video.parentElement.style.position = 'relative';
    video.parentElement.appendChild(button);
    
    video.parentElement.addEventListener('mouseenter', function() {
        button.style.opacity = '1';
    });
    
    video.parentElement.addEventListener('mouseleave', function() {
        button.style.opacity = '0';
    });
}

function animateNumber(element) {
    const target = parseInt(element.textContent);
    const duration = 2000;
    const step = target / (duration / 16);
    let current = 0;
    
    const timer = setInterval(() => {
        current += step;
        if (current >= target) {
            current = target;
            clearInterval(timer);
        }
        element.textContent = Math.floor(current);
    }, 16);
}

function showTooltip(element, text) {
    hideTooltip(); // 先隐藏之前的tooltip
    
    const tooltip = document.createElement('div');
    tooltip.className = 'custom-tooltip';
    tooltip.textContent = text;
    tooltip.style.cssText = `
        position: absolute;
        background: rgba(0, 0, 0, 0.8);
        color: white;
        padding: 8px 12px;
        border-radius: 6px;
        font-size: 12px;
        white-space: nowrap;
        z-index: 9999;
        pointer-events: none;
        opacity: 0;
        transition: opacity 0.3s ease;
    `;
    
    document.body.appendChild(tooltip);
    
    const rect = element.getBoundingClientRect();
    tooltip.style.left = rect.left + rect.width / 2 - tooltip.offsetWidth / 2 + 'px';
    tooltip.style.top = rect.top - tooltip.offsetHeight - 8 + 'px';
    
    setTimeout(() => {
        tooltip.style.opacity = '1';
    }, 10);
}

function hideTooltip() {
    const existing = document.querySelector('.custom-tooltip');
    if (existing) {
        existing.remove();
    }
}

// 页面性能优化
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// 图片懒加载
function initLazyLoading() {
    const images = document.querySelectorAll('img[data-src]');
    
    const imageObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.classList.remove('lazy');
                imageObserver.unobserve(img);
            }
        });
    });
    
    images.forEach(img => imageObserver.observe(img));
}

// CSS动画关键帧（在CSS中定义）
const style = document.createElement('style');
style.textContent = `
    @keyframes ripple {
        to {
            transform: scale(4);
            opacity: 0;
        }
    }
    
    .notification {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        border: none;
        border-radius: 8px;
    }
    
    .is-invalid {
        border-color: #dc3545 !important;
        box-shadow: 0 0 0 0.2rem rgba(220, 53, 69, 0.25) !important;
    }
    
    .is-valid {
        border-color: #28a745 !important;
        box-shadow: 0 0 0 0.2rem rgba(40, 167, 69, 0.25) !important;
    }
    
    .invalid-feedback {
        display: block;
        color: #dc3545;
        font-size: 0.875em;
        margin-top: 0.25rem;
    }
`;
document.head.appendChild(style);
