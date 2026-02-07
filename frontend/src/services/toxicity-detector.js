/**
 * Client-side Toxicity Detection using @mozilla/readability model
 * Complements server-side detection; runs locally before message send
 */

class ToxicityDetector {
  constructor(modelUrl = 'https://cdn.jsdelivr.net/npm/@tensorflow-models/toxicity@1.2.2/dist/toxicity.min.js') {
    this.modelUrl = modelUrl;
    this.model = null;
    this.labels = ['identity_attack', 'insult', 'obscene', 'severe_toxicity', 'sexual_explicit', 'threat', 'toxicity'];
    this.thresholds = { identity_attack: 0.5, insult: 0.5, obscene: 0.6, severe_toxicity: 0.7, sexual_explicit: 0.8, threat: 0.7, toxicity: 0.6 };
  }

  async init() {
    // Load TensorFlow.js toxicity model
    const script = document.createElement('script');
    script.src = 'https://cdn.jsdelivr.net/npm/@tensorflow/tfjs@3.12.0';
    document.head.appendChild(script);
    
    await new Promise(resolve => {
      script.onload = async () => {
        const toxicityScript = document.createElement('script');
        toxicityScript.src = this.modelUrl;
        document.head.appendChild(toxicityScript);
        toxicityScript.onload = async () => {
          this.model = await toxicity.load(0.9);
          resolve();
        };
      };
    });
  }

  async detectToxicity(text) {
    if (!this.model) await this.init();
    
    try {
      const predictions = await this.model.classify([text]);
      
      const results = {
        isToxic: false,
        score: 0,
        categories: {},
        severity: 'LOW'
      };

      predictions.forEach(pred => {
        const label = pred.label;
        const confidence = pred.results[0].match;
        results.categories[label] = confidence;

        // Check if above threshold
        if (confidence > (this.thresholds[label] || 0.5)) {
          results.isToxic = true;
          results.score = Math.max(results.score, confidence);
        }
      });

      // Determine severity
      if (results.score > 0.8) results.severity = 'HIGH';
      else if (results.score > 0.6) results.severity = 'MEDIUM';
      else results.severity = 'LOW';

      return results;
    } catch (err) {
      console.error('Toxicity detection error:', err);
      return { isToxic: false, score: 0, categories: {}, severity: 'LOW' };
    }
  }

  async filterMessage(text) {
    const detection = await this.detectToxicity(text);
    return {
      text,
      detection,
      isAllowed: !detection.isToxic || detection.severity === 'LOW',
      warning: detection.isToxic ? `Detected ${detection.severity} toxicity (${(detection.score * 100).toFixed(1)}%)` : null
    };
  }
}

module.exports = ToxicityDetector;
