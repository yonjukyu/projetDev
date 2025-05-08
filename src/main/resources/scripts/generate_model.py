import sys
import os
import torch
import json
from diffusers import ShapEPipeline
from diffusers.utils import export_to_obj, export_to_ply, export_to_gif

# Récupérer les arguments de la ligne de commande
model_id = "openai/shap-e"
prompt = sys.argv[1]
output_dir = sys.argv[2]
model_id = sys.argv[3] if len(sys.argv) > 3 else model_id

# Créer le répertoire de sortie s'il n'existe pas
os.makedirs(output_dir, exist_ok=True)

# Identifier le device (CUDA si disponible, sinon CPU)
device = "cuda" if torch.cuda.is_available() else "cpu"
print(f"Utilisation du device: {device}")

# Charger le modèle
pipe = ShapEPipeline.from_pretrained(model_id)
pipe = pipe.to(device)

# Générer le modèle 3D
print(f"Génération du modèle 3D pour le prompt: '{prompt}'")
guidance_scale = 15.0
images = pipe(
    prompt,
    guidance_scale=guidance_scale,
    num_inference_steps=64,
    size=256,
)

# Définir les chemins de sortie
base_filename = os.path.join(output_dir, prompt.replace(" ", "_"))
obj_path = f"{base_filename}.obj"
ply_path = f"{base_filename}.ply"
gif_path = f"{base_filename}.gif"

# Exporter les résultats dans différents formats
print("Exportation des résultats...")
export_to_obj(images, obj_path)
export_to_ply(images, ply_path)
gif_path = export_to_gif(images.images, gif_path)

# Compter le nombre de vertices et de faces
vertices_count = 0
faces_count = 0

try:
    with open(obj_path, 'r') as f:
        for line in f:
            if line.startswith('v '):
                vertices_count += 1
            elif line.startswith('f '):
                faces_count += 1
except Exception as e:
    print(f"Erreur lors du comptage des vertices et faces: {str(e)}")

# Créer un objet JSON avec les métadonnées
metadata = {
    "status": "completed",
    "prompt": prompt,
    "obj_path": obj_path,
    "ply_path": ply_path,
    "gif_path": gif_path,
    "stats": {
        "vertices": vertices_count,
        "faces": faces_count
    }
}

# Écrire les métadonnées dans un fichier JSON
metadata_path = f"{base_filename}_metadata.json"
with open(metadata_path, 'w') as f:
    json.dump(metadata, f, indent=4)

print(f"Métadonnées écrites dans: {metadata_path}")
print("Génération terminée avec succès!")

# Renvoyer le chemin du fichier de métadonnées (sera capturé par Java)
print(f"OUTPUT:{metadata_path}")