package gymgrind.game;

public record CharacterPoseAssets(
        String characterId,
        PoseAssetSet natural,
        PoseAssetSet steroid
) {

    public PoseAssetSet forForm(PoseForm form) {
        return form == PoseForm.STEROID ? steroid : natural;
    }
}
